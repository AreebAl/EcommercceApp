package com.ecommeceapp.ecommerceapp.catalog.category.dto;

import jakarta.validation.constraints.NotBlank;

public class CategoryDtos {

    public record CreateCategoryRequest(
            @NotBlank String name
    ) {}

    public record UpdateCategoryRequest(
            @NotBlank String name
    ) {}

    public record CategoryResponse(
            Long id,
            String name
    ) {}
}
