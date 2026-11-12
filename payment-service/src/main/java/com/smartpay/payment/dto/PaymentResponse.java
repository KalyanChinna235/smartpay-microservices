
package com.smartpay.payment.dto;

import com.smartpay.payment.entity.Payment;
import com.smartpay.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {

    private Long id;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private LocalDateTime createdAt;

    public PaymentResponse(Payment payment) {
        this.id = payment.getId();
        this.userId = payment.getUserId();
        this.amount = payment.getAmount();
        this.currency = payment.getCurrency();
        this.status = payment.getStatus();
        this.createdAt = payment.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
