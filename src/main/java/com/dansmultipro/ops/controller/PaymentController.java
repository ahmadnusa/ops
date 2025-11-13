package com.dansmultipro.ops.controller;

import com.dansmultipro.ops.constant.StatusTypeConstant;
import com.dansmultipro.ops.dto.common.ApiPostResponseDto;
import com.dansmultipro.ops.dto.common.ApiResponseDto;
import com.dansmultipro.ops.dto.payment.*;
import com.dansmultipro.ops.service.PaymentService;
import com.dansmultipro.ops.util.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final AuthUtil authUtil;

    public PaymentController(PaymentService paymentService, AuthUtil authUtil) {
        this.paymentService = paymentService;
        this.authUtil = authUtil;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiPostResponseDto> create(@Valid @RequestBody PaymentCreateRequestDto request) {
        ApiPostResponseDto response = paymentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/{status}")
    @PreAuthorize("hasAnyRole('CUSTOMER','GATEWAY')")
    public ResponseEntity<ApiResponseDto> updateStatus(
            @PathVariable String id,
            @PathVariable String status,
            @Valid @RequestBody(required = false) PaymentStatusUpdateRequestDto request) {
        ApiResponseDto response = paymentService.updateStatus(id, status, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SA','GATEWAY')")
    public ResponseEntity<PageResponseDto<PaymentResponseDto>> getAll(
            @RequestParam(required = false) StatusTypeConstant status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponseDto<PaymentResponseDto> response = paymentService.getAll(status, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PageResponseDto<PaymentCustomerResponseDto>> getAllByCustomer(
            @RequestParam(required = false) StatusTypeConstant status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UUID customerId = authUtil.getLoginId();
        PageResponseDto<PaymentCustomerResponseDto> response =
                paymentService.getAllByCustomer(customerId, status, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDto> getById(@PathVariable String id) {
        PaymentResponseDto response = paymentService.getById(id);
        return ResponseEntity.ok(response);
    }
}
