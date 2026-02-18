package com.ecommeceapp.ecommerceapp.payment.dto;

import com.ecommeceapp.ecommerceapp.payment.status.PaymentMethod;
import com.ecommeceapp.ecommerceapp.payment.status.PaymentStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public class PaymentDtos {

    public record CreatePaymentRequest(
            @NotNull Long orderId,
            @NotNull PaymentMethod method
    ) {}

    // simulate gateway callback / status update
    public record MarkPaymentRequest(
            @NotNull PaymentStatus status,
            String providerRef,
            String failureReason
    ) {}

    public record PaymentResponse(
            Long paymentId,
            Long orderId,
            PaymentMethod method,
            PaymentStatus status,
            BigDecimal amount,
            String providerRef,
            String failureReason,
            Instant createdAt,
            Instant updatedAt
    ) {}
}
