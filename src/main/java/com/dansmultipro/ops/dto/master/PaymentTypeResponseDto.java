package com.dansmultipro.ops.dto.master;

public record PaymentTypeResponseDto(
                String id,
                String code,
                String name,
                Boolean isActive,
                Integer optLock) {
}
