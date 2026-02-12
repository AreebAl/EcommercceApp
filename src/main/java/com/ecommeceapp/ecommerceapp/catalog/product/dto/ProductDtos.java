package com.ecommeceapp.ecommerceapp.catalog.product.dto;


import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ProductDtos {

    public record CreateProductRequest(
            @NotBlank String name,
            String description,
            @NotNull @Positive BigDecimal price,
            @NotNull @Min(0) Integer stock,
            String imageUrl,
            @NotNull Long categoryId
    ) {}

    public record UpdateProductRequest(
            @NotBlank String name,
            String description,
            @NotNull @Positive BigDecimal price,
            @NotNull @Min(0) Integer stock,
            String imageUrl,
            @NotNull Long categoryId,
            @NotNull Boolean active
    ) {}

    public record ProductResponse(
            Long id, String name, String description, BigDecimal price,
            Integer stock, String imageUrl, Long categoryId, String categoryName, Boolean active
    ) {}
}
