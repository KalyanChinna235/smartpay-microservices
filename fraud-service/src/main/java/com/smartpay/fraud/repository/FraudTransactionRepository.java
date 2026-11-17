
package com.smartpay.fraud.repository;

import com.smartpay.fraud.entity.FraudTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FraudTransactionRepository extends JpaRepository<FraudTransaction, Long> {

    Optional<FraudTransaction> findByPaymentId(Long paymentId);

    List<FraudTransaction> findByRiskLevelIgnoreCase(String riskLevel);
}
