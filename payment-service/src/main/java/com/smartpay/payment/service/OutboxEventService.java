
package com.smartpay.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartpay.payment.entity.OutboxEvent;
import com.smartpay.payment.entity.Payment;
import com.smartpay.payment.event.PaymentCreatedEvent;
import com.smartpay.payment.repository.OutboxEventRepository;

import org.springframework.stereotype.Service;

@Service
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxEventService(OutboxEventRepository outboxEventRepository,
                              ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    public void savePaymentCreatedEvent(Payment payment) {

        PaymentCreatedEvent event = new PaymentCreatedEvent(
                payment.getId(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus().name(),
                payment.getCreatedAt()
        );

        try {
            String payload = objectMapper.writeValueAsString(event);

            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setAggregateType("PAYMENT");
            outboxEvent.setAggregateId(payment.getId().toString());
            outboxEvent.setEventType("PAYMENT_CREATED");
            outboxEvent.setPayload(payload);
            outboxEvent.setStatus("PENDING");

            outboxEventRepository.save(outboxEvent);

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize payment event", exception);
        }
    }
}
