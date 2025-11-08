package com.dansmultipro.ops.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dansmultipro.ops.dto.master.RoleResponseDto;
import com.dansmultipro.ops.service.master.RoleService;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService service;

    public RoleController(RoleService service) {
        this.service = service;
    }

    @GetMapping
    public List<RoleResponseDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleResponseDto> findById(@PathVariable String id) {
        RoleResponseDto res = service.findById(id);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<RoleResponseDto> findByCode(@PathVariable String code) {
        RoleResponseDto res = service.findByCode(code);
        return ResponseEntity.ok(res);
    }
}
