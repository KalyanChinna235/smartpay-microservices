
package com.smartpay.payment.service;

import com.smartpay.payment.entity.Payment;
import com.smartpay.payment.entity.PaymentStatus;
import com.smartpay.payment.event.PaymentEventProducer;
import com.smartpay.payment.exception.IdempotencyConflictException;
import com.smartpay.payment.repository.PaymentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentWriter paymentWriter;
    private final PaymentEventProducer paymentEventProducer;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentWriter paymentWriter,
            PaymentEventProducer paymentEventProducer) {
        this.paymentRepository = paymentRepository;
        this.paymentWriter = paymentWriter;
        this.paymentEventProducer = paymentEventProducer;
    }

    public Payment createPayment(
            Long authenticatedUserId,
            Payment payment,
            String idempotencyKey) {

        if (authenticatedUserId == null || authenticatedUserId <= 0) {
            throw new IllegalArgumentException("Invalid authenticated user");
        }

        if (idempotencyKey == null || idempotencyKey.isBlank()
                || idempotencyKey.length() > 100) {
            throw new IllegalArgumentException("Invalid Idempotency-Key");
        }

        if (payment.getAmount() == null
                || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero");
        }

        if (payment.getCurrency() == null
                || !payment.getCurrency().matches("[A-Z]{3}")) {
            throw new IllegalArgumentException(
                    "Currency must be a 3-letter uppercase code, such as INR");
        }

        Optional<Payment> existingPayment =
                paymentRepository.findByUserIdAndIdempotencyKey(
                        authenticatedUserId, idempotencyKey);

        if (existingPayment.isPresent()) {
            return validateExistingPayment(existingPayment.get(), payment);
        }

        payment.setUserId(authenticatedUserId);
        payment.setIdempotencyKey(idempotencyKey);
        payment.setStatus(PaymentStatus.PENDING);

        try {
            Payment savedPayment = paymentWriter.savePayment(payment);

            // Publish only for a newly created payment.
            paymentEventProducer.publishPaymentCreatedEvent(savedPayment);

            return savedPayment;

        } catch (DataIntegrityViolationException exception) {

            // Another concurrent request may have inserted the same key.
            Payment concurrentPayment = paymentRepository
                    .findByUserIdAndIdempotencyKey(
                            authenticatedUserId, idempotencyKey)
                    .orElseThrow(() -> exception);

            return validateExistingPayment(concurrentPayment, payment);
        }
    }

    private Payment validateExistingPayment(
            Payment existing, Payment requested) {

        boolean sameRequest =
                existing.getAmount().compareTo(requested.getAmount()) == 0
                        && existing.getCurrency().equals(requested.getCurrency());

        if (!sameRequest) {
            throw new IdempotencyConflictException(
                    "Idempotency key already used with different payment details");
        }

        return existing;
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByUserId(
            Long requestedUserId, Long authenticatedUserId) {

        if (!authenticatedUserId.equals(requestedUserId)) {
            throw new AccessDeniedException(
                    "You are not authorized to access these payments");
        }

        return paymentRepository.findByUserId(authenticatedUserId);
    }

    @Transactional(readOnly = true)
    public Payment getPaymentById(
            Long paymentId, Long authenticatedUserId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Payment not found: " + paymentId));

        if (!payment.getUserId().equals(authenticatedUserId)) {
            throw new AccessDeniedException(
                    "You are not authorized to access this payment");
        }

        return payment;
    }
}
