
package com.smartpay.fraud.consumer;

import com.smartpay.fraud.entity.FraudTransaction;
import com.smartpay.fraud.event.PaymentCreatedEvent;
import com.smartpay.fraud.repository.FraudTransactionRepository;
import com.smartpay.fraud.service.FraudDetectionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final FraudTransactionRepository fraudTransactionRepository;
    private final FraudDetectionService fraudDetectionService;

    public PaymentEventConsumer(
            FraudTransactionRepository fraudTransactionRepository,
            FraudDetectionService fraudDetectionService) {

        this.fraudTransactionRepository = fraudTransactionRepository;
        this.fraudDetectionService = fraudDetectionService;
    }

    @Transactional
    @KafkaListener(
            topics = "payment-created",
            groupId = "fraud-service-group"
    )
    public void consumePaymentCreatedEvent(PaymentCreatedEvent event) {

        log.info("Payment event received. Payment ID: {}",
                event.paymentId());

        // Prevent duplicate processing
        if (fraudTransactionRepository
                .findByPaymentId(event.paymentId()).isPresent()) {

            log.info("Payment already processed. Payment ID: {}",
                    event.paymentId());
            return;
        }

        FraudTransaction transaction = new FraudTransaction();

        transaction.setPaymentId(event.paymentId());
        transaction.setUserId(event.userId());
        transaction.setAmount(event.amount());
        transaction.setCurrency(event.currency());
        transaction.setPaymentStatus(event.status());
        transaction.setPaymentCreatedAt(event.createdAt());

        // Apply fraud detection rules
        fraudDetectionService.evaluateTransaction(transaction);

        // Save evaluated transaction
        fraudTransactionRepository.save(transaction);

        log.info(
                "Fraud evaluation completed. Payment ID: {}, Risk Level: {}, Risk Score: {}",
                transaction.getPaymentId(),
                transaction.getRiskLevel(),
                transaction.getRiskScore()
        );
    }
}
