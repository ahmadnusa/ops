package com.dansmultipro.ops.dto.payment;

import jakarta.validation.constraints.NotBlank;

public record PaymentRejectRequestDto(
        @NotBlank(message = "Gateway note is required.")
        String gatewayNote) {
}
