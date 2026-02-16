package com.ecommeceapp.ecommerceapp.order.dto;



import com.ecommeceapp.ecommerceapp.order.status.OrderStatus;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderDtos {

    // For now, we keep address simple
    public record CheckoutRequest(
            @NotBlank String shippingAddress
    ) {}

    public record OrderItemResponse(
            Long productId,
            String productName,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal lineTotal
    ) {}

    public record OrderResponse(
            Long orderId,
            OrderStatus status,
            BigDecimal subtotal,
            BigDecimal total,
            Instant createdAt,
            String shippingAddress,
            List<OrderItemResponse> items
    ) {}
}
