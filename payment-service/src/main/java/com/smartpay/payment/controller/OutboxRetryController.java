
package com.smartpay.payment.controller;

import com.smartpay.payment.service.OutboxRetryService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/outbox")
public class OutboxRetryController {

    private final OutboxRetryService outboxRetryService;

    public OutboxRetryController(OutboxRetryService outboxRetryService) {
        this.outboxRetryService = outboxRetryService;
    }

    @PostMapping("/retry/{paymentId}")
    public ResponseEntity<Map<String, String>> retryFailedEvent(@PathVariable("paymentId") String paymentId) {

        outboxRetryService.retryFailedEvent(paymentId);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Outbox event queued for retry",
                        "paymentId", paymentId
                )
        );
    }
}
