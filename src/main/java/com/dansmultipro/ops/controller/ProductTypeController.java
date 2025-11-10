package com.dansmultipro.ops.controller;

import com.dansmultipro.ops.dto.master.ProductTypeResponseDto;
import com.dansmultipro.ops.service.ProductTypeService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/product-types")
@SecurityRequirement(name = "bearerAuth")
public class ProductTypeController {

    private final ProductTypeService service;

    public ProductTypeController(ProductTypeService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ProductTypeResponseDto>> findAll() {
        List<ProductTypeResponseDto> res = service.findAll();
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductTypeResponseDto> findById(@PathVariable String id) {
        ProductTypeResponseDto res = service.findById(id);
        return ResponseEntity.ok(res);
    }
}
