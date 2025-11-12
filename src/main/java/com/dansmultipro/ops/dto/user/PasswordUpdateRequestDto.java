package com.dansmultipro.ops.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PasswordUpdateRequestDto(
                @NotBlank(message = "Old password is required.")
                String oldPassword,
                @NotBlank(message = "New password is required.")
                String newPassword) {
}
