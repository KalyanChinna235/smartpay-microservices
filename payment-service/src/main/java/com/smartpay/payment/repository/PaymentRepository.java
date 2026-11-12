package com.smartpay.payment.repository;

import com.smartpay.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByUserId(Long userId);
    Optional<Payment> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);
}

