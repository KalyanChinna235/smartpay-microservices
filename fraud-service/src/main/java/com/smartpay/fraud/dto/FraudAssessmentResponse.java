
package com.smartpay.fraud.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FraudAssessmentResponse(
        Long paymentId,
        Long userId,
        BigDecimal amount,
        String currency,
        String paymentStatus,
        String riskLevel,
        Integer riskScore,
        String fraudReason,
        LocalDateTime paymentCreatedAt,
        LocalDateTime receivedAt
) {
}
