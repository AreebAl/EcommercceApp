package com.ecommeceapp.ecommerceapp.payment.controller;


import com.ecommeceapp.ecommerceapp.payment.dto.PaymentDtos;
import com.ecommeceapp.ecommerceapp.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService service;

    @PostMapping
    public ResponseEntity<PaymentDtos.PaymentResponse> create(Authentication auth,
                                                              @Valid @RequestBody PaymentDtos.CreatePaymentRequest req) {
        return ResponseEntity.ok(service.create(auth.getName(), req));
    }

    @PutMapping("/order/{orderId}")
    public ResponseEntity<PaymentDtos.PaymentResponse> mark(Authentication auth,
                                                            @PathVariable Long orderId,
                                                            @Valid @RequestBody PaymentDtos.MarkPaymentRequest req) {
        return ResponseEntity.ok(service.mark(auth.getName(), orderId, req));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentDtos.PaymentResponse> getByOrder(Authentication auth,
                                                                  @PathVariable Long orderId) {
        return ResponseEntity.ok(service.getByOrder(auth.getName(), orderId));
    }
}
