package com.ecommeceapp.ecommerceapp.catalog.product.service;

import com.ecommeceapp.ecommerceapp.catalog.category.*;
import com.ecommeceapp.ecommerceapp.catalog.category.entity.Category;
import com.ecommeceapp.ecommerceapp.catalog.category.repo.CategoryRepository;
import com.ecommeceapp.ecommerceapp.catalog.product.dto.ProductDtos;
import com.ecommeceapp.ecommerceapp.catalog.product.entity.Product;
import com.ecommeceapp.ecommerceapp.catalog.product.repo.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;

    @Transactional
    public ProductDtos.ProductResponse create(ProductDtos.CreateProductRequest req) {
        Category cat = categoryRepo.findById(req.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        Product p = Product.builder()
                .name(req.name())
                .description(req.description())
                .price(req.price())
                .stock(req.stock())
                .imageUrl(req.imageUrl())
                .category(cat)
                .active(true)
                .build();

        p = productRepo.save(p);
        return toResponse(p);
    }

    public Page<ProductDtos.ProductResponse> list(String q, Pageable pageable) {
        Page<Product> page = (q == null || q.isBlank())
                ? productRepo.findByActiveTrue(pageable)
                : productRepo.findByActiveTrueAndNameContainingIgnoreCase(q, pageable);

        return page.map(this::toResponse);
    }

    public ProductDtos.ProductResponse get(Long id) {
        Product p = productRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        return toResponse(p);
    }

    @Transactional
    public ProductDtos.ProductResponse update(Long id, ProductDtos.UpdateProductRequest req) {
        Product p = productRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Category cat = categoryRepo.findById(req.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        p.setName(req.name());
        p.setDescription(req.description());
        p.setPrice(req.price());
        p.setStock(req.stock());
        p.setImageUrl(req.imageUrl());
        p.setCategory(cat);
        p.setActive(req.active());

        return toResponse(p);
    }

    private ProductDtos.ProductResponse toResponse(Product p) {
        Long catId = p.getCategory() != null ? p.getCategory().getId() : null;
        String catName = p.getCategory() != null ? p.getCategory().getName() : null;
        return new ProductDtos.ProductResponse(
                p.getId(), p.getName(), p.getDescription(), p.getPrice(),
                p.getStock(), p.getImageUrl(), catId, catName, p.getActive()
        );
    }
}
