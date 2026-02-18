package com.ecommeceapp.ecommerceapp.admin.order;




import com.ecommeceapp.ecommerceapp.order.status.OrderStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class AdminOrderDtos {

    public record UpdateOrderStatusRequest(
            @NotNull OrderStatus status
    ) {}

    public record AdminOrderItemResponse(
            Long productId,
            String productName,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal lineTotal
    ) {}

    public record AdminOrderResponse(
            Long orderId,
            String customerEmail,
            OrderStatus status,
            BigDecimal subtotal,
            BigDecimal total,
            Instant createdAt,
            String shippingAddress,
            List<AdminOrderItemResponse> items
    ) {}
}
