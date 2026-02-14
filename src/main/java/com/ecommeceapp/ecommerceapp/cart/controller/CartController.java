package com.ecommeceapp.ecommerceapp.cart.controller;

import com.ecommeceapp.ecommerceapp.cart.dto.CartDtos;
import com.ecommeceapp.ecommerceapp.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {

    private final CartService service;

    @GetMapping
    public ResponseEntity<CartDtos.CartResponse> getMyCart(Authentication auth) {
        return ResponseEntity.ok(service.getMyCart(auth.getName()));
    }

    @PostMapping("/items")
    public ResponseEntity<CartDtos.CartResponse> addItem(Authentication auth,
                                                         @Valid @RequestBody CartDtos.AddToCartRequest req) {
        return ResponseEntity.ok(service.addItem(auth.getName(), req));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartDtos.CartResponse> updateQty(Authentication auth,
                                                           @PathVariable Long itemId,
                                                           @Valid @RequestBody CartDtos.UpdateCartItemQtyRequest req) {
        return ResponseEntity.ok(service.updateQty(auth.getName(), itemId, req));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> removeItem(Authentication auth, @PathVariable Long itemId) {
        service.removeItem(auth.getName(), itemId);
        return ResponseEntity.noContent().build(); // 204
    }

    @DeleteMapping
    public ResponseEntity<?> clear(Authentication auth) {
        service.clearCart(auth.getName());
        return ResponseEntity.noContent().build(); // 204
    }
}
