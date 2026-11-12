package com.smartpay.payment.event;

import com.smartpay.payment.entity.Payment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventProducer {

    private static final String TOPIC = "payment-created";

    private final KafkaTemplate<String, PaymentCreatedEvent> kafkaTemplate;

    public PaymentEventProducer(KafkaTemplate<String, PaymentCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPaymentCreatedEvent(Payment payment) {

        PaymentCreatedEvent event = new PaymentCreatedEvent(
                payment.getId(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus().name(),
                payment.getCreatedAt()
        );

        kafkaTemplate.send(
                TOPIC,
                payment.getId().toString(),
                event
        );
    }
}