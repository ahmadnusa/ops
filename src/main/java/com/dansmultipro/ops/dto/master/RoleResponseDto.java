package com.dansmultipro.ops.dto.master;

public record RoleResponseDto(
        String id,
        String code,
        String name,
        Boolean isActive,
        Integer optLock
) {
}
