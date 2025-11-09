package com.dansmultipro.ops.controller;

import com.dansmultipro.ops.constant.StatusTypeConstant;
import com.dansmultipro.ops.dto.common.ApiDeleteResponseDto;
import com.dansmultipro.ops.dto.common.ApiPostResponseDto;
import com.dansmultipro.ops.dto.common.ApiPutResponseDto;
import com.dansmultipro.ops.dto.payment.PaymentCreateRequestDto;
import com.dansmultipro.ops.dto.payment.PaymentRejectRequestDto;
import com.dansmultipro.ops.dto.payment.PaymentResponseDto;
import com.dansmultipro.ops.dto.payment.PaymentUpdateRequestDto;
import com.dansmultipro.ops.service.PaymentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<ApiPostResponseDto> create(@Valid @RequestBody PaymentCreateRequestDto request) {
        ApiPostResponseDto response = paymentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiPutResponseDto> update(
            @PathVariable String id,
            @Valid @RequestBody PaymentUpdateRequestDto request) {
        ApiPutResponseDto response = paymentService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiDeleteResponseDto> cancel(@PathVariable String id) {
        ApiDeleteResponseDto response = paymentService.cancel(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiPutResponseDto> approve(@PathVariable String id) {
        ApiPutResponseDto response = paymentService.approve(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiPutResponseDto> reject(
            @PathVariable String id,
            @Valid @RequestBody PaymentRejectRequestDto request) {
        ApiPutResponseDto response = paymentService.reject(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<PaymentResponseDto>> getAll(
            @RequestParam(required = false) StatusTypeConstant status,
            Pageable pageable) {
        Page<PaymentResponseDto> response = paymentService.getAll(status, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDto> getById(@PathVariable String id) {
        PaymentResponseDto response = paymentService.getById(id);
        return ResponseEntity.ok(response);
    }
}
