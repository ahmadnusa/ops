package com.dansmultipro.ops.dto.notification;

public record EmailNotificationMessageDto(
                String email,
                PaymentEmailPayload payment,
                String temporaryPassword) {
}
