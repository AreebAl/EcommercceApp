package com.ecommeceapp.ecommerceapp.cart.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class CartDtos {

    public record AddToCartRequest(
            @NotNull Long productId,
            @NotNull @Min(1) Integer quantity
    ) {}

    public record UpdateCartItemQtyRequest(
            @NotNull @Min(1) Integer quantity
    ) {}

    public record CartItemResponse(
            Long itemId,
            Long productId,
            String productName,
            BigDecimal price,
            Integer quantity,
            BigDecimal lineTotal
    ) {}

    public record CartResponse(
            Long cartId,
            List<CartItemResponse> items,
            BigDecimal subtotal
    ) {}
}
