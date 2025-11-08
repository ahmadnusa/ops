package com.dansmultipro.ops.dto.master;

public record ProductTypeResponseDto(
                String id,
                String code,
                String name,
                Boolean isActive,
                Integer optLock) {
}
