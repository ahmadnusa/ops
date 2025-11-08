package com.dansmultipro.ops.dto.master;

public record StatusTypeResponseDto(
        String id,
        String code,
        String name,
        Boolean isActive,
        Integer optLock
) {
}
