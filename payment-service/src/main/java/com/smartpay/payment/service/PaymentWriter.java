
package com.smartpay.payment.service;

import com.smartpay.payment.entity.Payment;
import com.smartpay.payment.repository.PaymentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentWriter {

    private final PaymentRepository paymentRepository;
    private final OutboxEventService outboxEventService;

    public PaymentWriter(
            PaymentRepository paymentRepository,
            OutboxEventService outboxEventService) {

        this.paymentRepository = paymentRepository;
        this.outboxEventService = outboxEventService;
    }

    @Transactional
    public Payment savePayment(Payment payment) {

        // 1. Save payment to MySQL
        Payment savedPayment = paymentRepository.saveAndFlush(payment);

        // 2. Save event to outbox table
        outboxEventService.savePaymentCreatedEvent(savedPayment);

        return savedPayment;
    }
}
