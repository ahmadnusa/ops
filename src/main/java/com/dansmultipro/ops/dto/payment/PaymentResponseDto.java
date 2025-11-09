package com.dansmultipro.ops.dto.payment;

import java.math.BigDecimal;

public record PaymentResponseDto(
        String id,
        String customerId,
        String customerName,
        String productType,
        String paymentType,
        String customerNumber,
        BigDecimal amount,
        String status,
        String referenceNo,
        Boolean isActive,
        Integer optLock) {
}
