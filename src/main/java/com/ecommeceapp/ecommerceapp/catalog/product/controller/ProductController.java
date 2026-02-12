package com.ecommeceapp.ecommerceapp.catalog.product.controller;


import com.ecommeceapp.ecommerceapp.catalog.product.dto.ProductDtos;
import com.ecommeceapp.ecommerceapp.catalog.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    @PostMapping
    public ResponseEntity<ProductDtos.ProductResponse> create(@Valid @RequestBody ProductDtos.CreateProductRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @GetMapping
    public ResponseEntity<Page<ProductDtos.ProductResponse>> list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(service.list(q, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDtos.ProductResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDtos.ProductResponse> update(@PathVariable Long id,
                                                              @Valid @RequestBody ProductDtos.UpdateProductRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }
}
