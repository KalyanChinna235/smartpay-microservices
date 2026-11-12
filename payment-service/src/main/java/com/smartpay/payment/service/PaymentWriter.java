package com.smartpay.payment.service;

import com.smartpay.payment.entity.Payment;
import com.smartpay.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentWriter {

    private final PaymentRepository paymentRepository;

    public PaymentWriter(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Payment savePayment(Payment payment) {
        return paymentRepository.saveAndFlush(payment);
    }
}