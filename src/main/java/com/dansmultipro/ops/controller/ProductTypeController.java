package com.dansmultipro.ops.controller;

import com.dansmultipro.ops.dto.master.ProductTypeResponseDto;
import com.dansmultipro.ops.service.master.ProductTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/product-types")
public class ProductTypeController {

    private final ProductTypeService service;

    public ProductTypeController(ProductTypeService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProductTypeResponseDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductTypeResponseDto> findById(@PathVariable String id) {
        ProductTypeResponseDto res = service.findById(id);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ProductTypeResponseDto> findByCode(@PathVariable String code) {
        ProductTypeResponseDto res = service.findByCode(code);
        return ResponseEntity.ok(res);
    }
}
