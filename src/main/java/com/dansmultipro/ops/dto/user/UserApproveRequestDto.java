package com.dansmultipro.ops.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UserApproveRequestDto(
        @NotEmpty(message = "userIds must not be empty")
        List<@NotBlank(message = "userId must not be blank") String> userIds
) {
}
