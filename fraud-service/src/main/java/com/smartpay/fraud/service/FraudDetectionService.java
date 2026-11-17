
package com.smartpay.fraud.service;

import com.smartpay.fraud.entity.FraudTransaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class FraudDetectionService {

    public void evaluateTransaction(FraudTransaction transaction) {

        BigDecimal amount = transaction.getAmount();

        if (amount.compareTo(new BigDecimal("50000")) > 0) {

            transaction.setRiskLevel("HIGH");
            transaction.setRiskScore(90);
            transaction.setFraudReason(
                    "Transaction amount exceeds INR 50,000");

        } else if (amount.compareTo(new BigDecimal("20000")) > 0) {

            transaction.setRiskLevel("MEDIUM");
            transaction.setRiskScore(60);
            transaction.setFraudReason(
                    "Transaction amount exceeds INR 20,000");

        } else {

            transaction.setRiskLevel("LOW");
            transaction.setRiskScore(10);
            transaction.setFraudReason(
                    "Transaction amount is within normal limits");
        }
    }
}
