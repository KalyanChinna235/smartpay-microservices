
package com.smartpay.fraud.controller;

import com.smartpay.fraud.dto.FraudAssessmentResponse;
import com.smartpay.fraud.entity.FraudTransaction;
import com.smartpay.fraud.repository.FraudTransactionRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fraud/transactions")
public class FraudController {

    private final FraudTransactionRepository fraudTransactionRepository;

    public FraudController(
            FraudTransactionRepository fraudTransactionRepository) {
        this.fraudTransactionRepository = fraudTransactionRepository;
    }

    @GetMapping
    public ResponseEntity<List<FraudAssessmentResponse>> getAllAssessments() {

        List<FraudAssessmentResponse> responses =
                fraudTransactionRepository.findAll()
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<FraudAssessmentResponse> getAssessmentByPaymentId(@PathVariable("paymentId") Long paymentId) {

        FraudTransaction transaction = fraudTransactionRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Fraud assessment not found for payment ID: " + paymentId
                ));

        return ResponseEntity.ok(mapToResponse(transaction));
    }

    @GetMapping("/risk/{riskLevel}")
    public ResponseEntity<List<FraudAssessmentResponse>> getAssessmentsByRiskLevel(@PathVariable("riskLevel") String riskLevel) {

        if (!List.of("HIGH", "MEDIUM", "LOW")
                .contains(riskLevel.toUpperCase())) {
            throw new IllegalArgumentException(
                    "Invalid risk level. Allowed values: HIGH, MEDIUM, LOW");
        }

        List<FraudAssessmentResponse> responses = fraudTransactionRepository
                .findByRiskLevelIgnoreCase(riskLevel)
                .stream()
                .map(this::mapToResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    private FraudAssessmentResponse mapToResponse(FraudTransaction transaction) {

        return new FraudAssessmentResponse(
                transaction.getPaymentId(),
                transaction.getUserId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getPaymentStatus(),
                transaction.getRiskLevel(),
                transaction.getRiskScore(),
                transaction.getFraudReason(),
                transaction.getPaymentCreatedAt(),
                transaction.getReceivedAt()
        );
    }
}
