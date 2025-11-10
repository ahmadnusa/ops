package com.dansmultipro.ops.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dansmultipro.ops.dto.master.RoleResponseDto;
import com.dansmultipro.ops.service.RoleService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/roles")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

    private final RoleService service;

    public RoleController(RoleService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<RoleResponseDto>> findAll() {
        List<RoleResponseDto> res = service.findAll();
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleResponseDto> findById(@PathVariable String id) {
        RoleResponseDto res = service.findById(id);
        return ResponseEntity.ok(res);
    }
}
