package com.dansmultipro.ops.dto.payment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PaymentCreateRequestDto(
        @NotBlank(message = "Product type is required.")
        String productType,
        @NotBlank(message = "Payment type is required.")
        String paymentType,
        @NotBlank(message = "Customer number is required.")
        String customerNumber,
        @NotNull(message = "Amount is required.")
        @Min(value = 0, message = "Amount must be greater than or equal to zero.")
        BigDecimal amount,
        String description) {
}
