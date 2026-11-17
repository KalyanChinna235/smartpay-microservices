
package com.smartpay.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartpay.payment.entity.OutboxEvent;
import com.smartpay.payment.event.PaymentCreatedEvent;
import com.smartpay.payment.repository.OutboxEventRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OutboxEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventPublisher.class);

    private static final String TOPIC = "payment-created";
    private static final int MAX_RETRIES = 5;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, PaymentCreatedEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxEventPublisher(
            OutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, PaymentCreatedEvent> kafkaTemplate,
            ObjectMapper objectMapper) {

        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {

        List<OutboxEvent> pendingEvents = outboxEventRepository.findEventsReadyForPublishing(
                "PENDING",
                LocalDateTime.now()
        );

        log.info(
                "Outbox publisher running. Pending events found: {}",
                pendingEvents.size()
        );

        for (OutboxEvent event : pendingEvents) {

            try {
                PaymentCreatedEvent paymentEvent = objectMapper.readValue(event.getPayload(),
                        PaymentCreatedEvent.class
                );

                kafkaTemplate.send(
                        TOPIC,
                        event.getAggregateId(),
                        paymentEvent
                ).get();

                // Publishing successful
                event.setStatus("PUBLISHED");
                event.setPublishedAt(LocalDateTime.now());

                // Clear previous retry details
                event.setLastError(null);
                event.setNextRetryAt(null);

                outboxEventRepository.save(event);

                log.info(
                        "Outbox event published successfully. Event ID: {}, Payment ID: {}",
                        event.getId(),
                        event.getAggregateId()
                );

            } catch (Exception exception) {

                handlePublishingFailure(event, exception);
            }
        }
    }

    private void handlePublishingFailure(
            OutboxEvent event,
            Exception exception) {

        int retryCount = event.getRetryCount() + 1;

        event.setRetryCount(retryCount);
        event.setLastError(exception.getMessage());

        if (retryCount >= MAX_RETRIES) {

            event.setStatus("FAILED");
            event.setNextRetryAt(null);

            // Preserve lastError for troubleshooting
            log.error(
                    "Outbox event permanently failed. Event ID: {}, Attempts: {}, Error: {}",
                    event.getId(),
                    retryCount,
                    exception.getMessage()
            );

        } else {

            // Exponential backoff: 5, 10, 20, 40 seconds
            long delaySeconds = 5L * (1L << (retryCount - 1));

            event.setNextRetryAt(
                    LocalDateTime.now().plusSeconds(delaySeconds)
            );

            log.warn(
                    "Outbox event retry scheduled. Event ID: {}, Attempt: {}, Next retry in {} seconds",
                    event.getId(),
                    retryCount,
                    delaySeconds
            );
        }

        outboxEventRepository.save(event);
    }
}
