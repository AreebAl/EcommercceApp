package com.ecommeceapp.ecommerceapp.catalog.category.service;

import com.ecommeceapp.ecommerceapp.catalog.category.dto.CategoryDtos;
import com.ecommeceapp.ecommerceapp.catalog.category.entity.Category;
import com.ecommeceapp.ecommerceapp.catalog.category.repo.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;



@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepo;

    @Transactional
    public CategoryDtos.CategoryResponse create(CategoryDtos.CreateCategoryRequest req) {
        categoryRepo.findByNameIgnoreCase(req.name())
                .ifPresent(c -> { throw new IllegalArgumentException("Category already exists"); });

        Category c = Category.builder()
                .name(req.name().trim())
                .build();

        c = categoryRepo.save(c);
        return toResponse(c);
    }

    public List<CategoryDtos.CategoryResponse> list() {
        return categoryRepo.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CategoryDtos.CategoryResponse get(Long id) {
        Category c = categoryRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        return toResponse(c);
    }

    @Transactional
    public CategoryDtos.CategoryResponse update(Long id, CategoryDtos.UpdateCategoryRequest req) {
        Category c = categoryRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        categoryRepo.findByNameIgnoreCase(req.name())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new IllegalArgumentException("Category name already exists");
                    }
                });

        c.setName(req.name().trim());
        return toResponse(c);
    }

    @Transactional
    public void delete(Long id) {
        if (!categoryRepo.existsById(id)) {
            throw new IllegalArgumentException("Category not found");
        }
        categoryRepo.deleteById(id);
    }

    private CategoryDtos.CategoryResponse toResponse(Category c) {
        return new CategoryDtos.CategoryResponse(c.getId(), c.getName());
    }
}

