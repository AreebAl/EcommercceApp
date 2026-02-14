package com.ecommeceapp.ecommerceapp.cart.service;




import com.ecommeceapp.ecommerceapp.cart.dto.CartDtos;
import com.ecommeceapp.ecommerceapp.cart.entity.Cart;
import com.ecommeceapp.ecommerceapp.cart.entity.CartItem;
import com.ecommeceapp.ecommerceapp.cart.repo.CartItemRepository;
import com.ecommeceapp.ecommerceapp.cart.repo.CartRepository;
import com.ecommeceapp.ecommerceapp.catalog.product.entity.Product;
import com.ecommeceapp.ecommerceapp.catalog.product.repo.ProductRepository;
import com.ecommeceapp.ecommerceapp.common.exception.ResourceNotFoundException;
import com.ecommeceapp.ecommerceapp.user.entity.User;
import com.ecommeceapp.ecommerceapp.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;



@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepo;
    private final CartItemRepository cartItemRepo;
    private final UserRepository userRepo;
    private final ProductRepository productRepo;

    private User getUserByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private Cart getOrCreateCart(User user) {
        return cartRepo.findByUserId(user.getId()).orElseGet(() -> {
            Cart c = Cart.builder().user(user).build();
            return cartRepo.save(c);
        });
    }

    @Transactional(readOnly = true)
    public CartDtos.CartResponse getMyCart(String email) {
        User user = getUserByEmail(email);
        Cart cart = cartRepo.findByUserId(user.getId()).orElse(null);

        if (cart == null) {
            return new CartDtos.CartResponse(null, java.util.List.of(), BigDecimal.ZERO);
        }
        return toResponse(cart);
    }

    @Transactional
    public CartDtos.CartResponse addItem(String email, CartDtos.AddToCartRequest req) {
        User user = getUserByEmail(email);
        Cart cart = getOrCreateCart(user);

        Product product = productRepo.findById(req.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + req.productId()));

        if (Boolean.FALSE.equals(product.getActive())) {
            throw new IllegalArgumentException("Product is not active");
        }

        if (product.getStock() < req.quantity()) {
            throw new IllegalArgumentException("Not enough stock");
        }

        // if item exists, increment quantity
        CartItem existing = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            int newQty = existing.getQuantity() + req.quantity();
            if (product.getStock() < newQty) {
                throw new IllegalArgumentException("Not enough stock for total quantity");
            }
            existing.setQuantity(newQty);
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(req.quantity())
                    .build();
            cart.getItems().add(item);
        }

        Cart saved = cartRepo.save(cart);
        return toResponse(saved);
    }

    @Transactional
    public CartDtos.CartResponse updateQty(String email, Long itemId, CartDtos.UpdateCartItemQtyRequest req) {
        User user = getUserByEmail(email);
        Cart cart = cartRepo.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        CartItem item = cartItemRepo.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + itemId));

        Product product = item.getProduct();
        if (product.getStock() < req.quantity()) {
            throw new IllegalArgumentException("Not enough stock");
        }

        item.setQuantity(req.quantity());
        cartItemRepo.save(item);

        return toResponse(cart);
    }

    @Transactional
    public void removeItem(String email, Long itemId) {
        User user = getUserByEmail(email);
        Cart cart = cartRepo.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        CartItem item = cartItemRepo.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + itemId));

        cart.getItems().remove(item);
        cartRepo.save(cart); // orphanRemoval deletes item
    }

    @Transactional
    public void clearCart(String email) {
        User user = getUserByEmail(email);
        Cart cart = cartRepo.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        cart.getItems().clear();
        cartRepo.save(cart);
    }

    private CartDtos.CartResponse toResponse(Cart cart) {
        var items = cart.getItems().stream().map(i -> {
            BigDecimal price = i.getProduct().getPrice();
            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(i.getQuantity()));
            return new CartDtos.CartItemResponse(
                    i.getId(),
                    i.getProduct().getId(),
                    i.getProduct().getName(),
                    price,
                    i.getQuantity(),
                    lineTotal
            );
        }).toList();

        BigDecimal subtotal = items.stream()
                .map(CartDtos.CartItemResponse
                        ::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartDtos.CartResponse(cart.getId(), items, subtotal);
    }
}
