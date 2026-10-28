
package com.smartpay.payment.service;

import com.smartpay.payment.entity.Payment;
import com.smartpay.payment.entity.PaymentStatus;
import com.smartpay.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Payment createPayment(Payment payment) {

        if (payment.getUserId() == null || payment.getUserId() <= 0) {
            throw new IllegalArgumentException("Valid user ID is required");
        }

        if (payment.getAmount() == null
                || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        if (payment.getCurrency() == null
                || !payment.getCurrency().matches("[A-Z]{3}")) {
            throw new IllegalArgumentException(
                    "Currency must be a 3-letter uppercase code, such as INR");
        }

        payment.setStatus(PaymentStatus.PENDING);

        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Valid user ID is required");
        }

        return paymentRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Payment not found: " + paymentId));
    }
}
