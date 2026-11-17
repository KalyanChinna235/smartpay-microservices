
package com.smartpay.payment.service;

import com.smartpay.payment.entity.OutboxEvent;
import com.smartpay.payment.repository.OutboxEventRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OutboxRetryService {

    private final OutboxEventRepository outboxEventRepository;

    public OutboxRetryService(OutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
    }

    @Transactional
    public void retryFailedEvent(String paymentId) {

        OutboxEvent event = outboxEventRepository
                .findByAggregateIdAndStatus(paymentId, "FAILED")
                .orElseThrow(() -> new IllegalArgumentException(
                        "Failed outbox event not found for payment ID: " + paymentId
                ));

        event.setStatus("PENDING");
        event.setRetryCount(0);
        event.setLastError(null);
        event.setNextRetryAt(null);
        event.setPublishedAt(null);

        outboxEventRepository.save(event);
    }
}
