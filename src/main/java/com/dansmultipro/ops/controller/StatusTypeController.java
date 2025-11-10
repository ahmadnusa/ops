package com.dansmultipro.ops.controller;

import com.dansmultipro.ops.dto.master.StatusTypeResponseDto;
import com.dansmultipro.ops.service.StatusTypeService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/status-types")
@SecurityRequirement(name = "bearerAuth")
public class StatusTypeController {

    private final StatusTypeService service;

    public StatusTypeController(StatusTypeService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<StatusTypeResponseDto>> findAll() {
        List<StatusTypeResponseDto> res = service.findAll();
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StatusTypeResponseDto> findById(@PathVariable String id) {
        StatusTypeResponseDto res = service.findById(id);
        return ResponseEntity.ok(res);
    }
}
