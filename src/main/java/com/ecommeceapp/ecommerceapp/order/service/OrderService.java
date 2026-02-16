package com.ecommeceapp.ecommerceapp.order.service;




import com.ecommeceapp.ecommerceapp.cart.entity.Cart;
import com.ecommeceapp.ecommerceapp.cart.repo.CartRepository;
import com.ecommeceapp.ecommerceapp.catalog.product.entity.Product;
import com.ecommeceapp.ecommerceapp.catalog.product.repo.ProductRepository;
import com.ecommeceapp.ecommerceapp.common.exception.ResourceNotFoundException;
import com.ecommeceapp.ecommerceapp.order.dto.OrderDtos;
import com.ecommeceapp.ecommerceapp.order.entity.Order;
import com.ecommeceapp.ecommerceapp.order.entity.OrderItem;
import com.ecommeceapp.ecommerceapp.order.repo.OrderRepository;
import com.ecommeceapp.ecommerceapp.order.status.OrderStatus;
import com.ecommeceapp.ecommerceapp.user.entity.User;
import com.ecommeceapp.ecommerceapp.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;


@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepo;
    private final CartRepository cartRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;

    private User getUserByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    @Transactional
    public OrderDtos.OrderResponse checkout(String email, OrderDtos.CheckoutRequest req) {
        User user = getUserByEmail(email);

        Cart cart = cartRepo.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        // 1) Validate stock and compute totals
        BigDecimal subtotal = BigDecimal.ZERO;

        for (var item : cart.getItems()) {
            Product p = item.getProduct();
            if (Boolean.FALSE.equals(p.getActive())) {
                throw new ResourceNotFoundException("Product not active: " + p.getId());
            }
            if (p.getStock() < item.getQuantity()) {
                throw new ResourceNotFoundException("Not enough stock for product: " + p.getName());
            }
            subtotal = subtotal.add(p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        BigDecimal total = subtotal; // later: add shipping/tax/discount

        // 2) Create order
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING_PAYMENT)
                .subtotal(subtotal)
                .total(total)
                .shippingAddress(req.shippingAddress())
                .build();

        // 3) Create order items + reduce stock
        for (var ci : cart.getItems()) {
            Product p = ci.getProduct();

            int newStock = p.getStock() - ci.getQuantity();
            p.setStock(newStock);
            productRepo.save(p);

            BigDecimal lineTotal = p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));

            OrderItem oi = OrderItem.builder()
                    .order(order)
                    .product(p)
                    .productName(p.getName())
                    .unitPrice(p.getPrice())
                    .quantity(ci.getQuantity())
                    .lineTotal(lineTotal)
                    .build();

            order.getItems().add(oi);
        }

        Order saved = orderRepo.save(order);

        // 4) Clear cart
        cart.getItems().clear();
        cartRepo.save(cart);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<OrderDtos.OrderResponse> myOrders(String email, Pageable pageable) {
        User user = getUserByEmail(email);
        return orderRepo.findByUserId(user.getId(), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public OrderDtos.OrderResponse getMyOrder(String email, Long orderId) {
        User user = getUserByEmail(email);

        Order o = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (!o.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Not allowed to access this order");
        }

        return toResponse(o);
    }

    private OrderDtos.OrderResponse toResponse(Order o) {
        var items = o.getItems().stream()
                .map(i -> new OrderDtos.OrderItemResponse(
                        i.getProduct().getId(),
                        i.getProductName(),
                        i.getUnitPrice(),
                        i.getQuantity(),
                        i.getLineTotal()
                ))
                .toList();

        return new OrderDtos.OrderResponse(
                o.getId(),
                o.getStatus(),
                o.getSubtotal(),
                o.getTotal(),
                o.getCreatedAt(),
                o.getShippingAddress(),
                items
        );
    }
}

