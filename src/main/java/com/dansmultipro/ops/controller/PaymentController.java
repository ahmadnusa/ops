package com.dansmultipro.ops.controller;

import com.dansmultipro.ops.dto.common.ApiDeleteResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dansmultipro.ops.constant.StatusTypeConstant;
import com.dansmultipro.ops.dto.common.ApiPostResponseDto;
import com.dansmultipro.ops.dto.common.ApiPutResponseDto;
import com.dansmultipro.ops.dto.payment.PageResponseDto;
import com.dansmultipro.ops.dto.payment.PaymentCreateRequestDto;
import com.dansmultipro.ops.dto.payment.PaymentCustomerResponseDto;
import com.dansmultipro.ops.dto.payment.PaymentResponseDto;
import com.dansmultipro.ops.dto.payment.PaymentStatusUpdateRequestDto;
import com.dansmultipro.ops.service.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiPostResponseDto> create(@Valid @RequestBody PaymentCreateRequestDto request) {
        ApiPostResponseDto response = paymentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/{status}")
    @PreAuthorize("hasAnyRole('CUSTOMER','GATEWAY')")
    public ResponseEntity<ApiDeleteResponseDto> updateStatus(
            @PathVariable String id,
            @PathVariable String status,
            @Valid @RequestBody(required = false) PaymentStatusUpdateRequestDto request) {
        ApiDeleteResponseDto response = paymentService.updateStatus(id, status, request);
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
        PageResponseDto<PaymentCustomerResponseDto> response = paymentService.getAllByCustomer(status, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDto> getById(@PathVariable String id) {
        PaymentResponseDto response = paymentService.getById(id);
        return ResponseEntity.ok(response);
    }
}
