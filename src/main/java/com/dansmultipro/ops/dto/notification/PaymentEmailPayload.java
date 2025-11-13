package com.dansmultipro.ops.dto.notification;

public record PaymentEmailPayload(
                String paymentId,
                String customerFullName,
                String customerNumber,
                String statusCode,
                String gatewayNote,
                String referenceNo) {
}

