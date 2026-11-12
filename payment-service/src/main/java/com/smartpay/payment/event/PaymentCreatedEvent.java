package com.smartpay.payment.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentCreatedEvent(
        Long paymentId,
        Long userId,
        BigDecimal amount,
        String currency,
        String status,
        LocalDateTime createdAt
) {
}