package com.ecommeceapp.ecommerceapp.order.controller;


import com.ecommeceapp.ecommerceapp.order.dto.OrderDtos;
import com.ecommeceapp.ecommerceapp.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    @PostMapping("/checkout")
    public ResponseEntity<OrderDtos.OrderResponse> checkout(Authentication auth,
                                                            @Valid @RequestBody OrderDtos.CheckoutRequest req) {
        return ResponseEntity.ok(service.checkout(auth.getName(), req));
    }

    @GetMapping("/my")
    public ResponseEntity<Page<OrderDtos.OrderResponse>> myOrders(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(service.myOrders(auth.getName(), pageable));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDtos.OrderResponse> getOrder(Authentication auth,
                                                            @PathVariable Long orderId) {
        return ResponseEntity.ok(service.getMyOrder(auth.getName(), orderId));
    }
}
