
package com.smartpay.payment.controller;

import com.smartpay.payment.dto.PaymentRequest;
import com.smartpay.payment.dto.PaymentResponse;
import com.smartpay.payment.entity.Payment;
import com.smartpay.payment.security.AuthenticatedUser;
import com.smartpay.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request,
                                                         @RequestHeader("Idempotency-Key") String idempotencyKey,
                                                         @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        Payment payment = new Payment();
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());

        Payment createdPayment = paymentService.createPayment(
                authenticatedUser.userId(),
                payment,
                idempotencyKey
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new PaymentResponse(createdPayment));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable("paymentId") Long paymentId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        return ResponseEntity.ok(
                new PaymentResponse(
                        paymentService.getPaymentById(
                                paymentId, authenticatedUser.userId())));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUserId(
            @PathVariable("userId") Long userId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        List<PaymentResponse> response = paymentService
                .getPaymentsByUserId(userId, authenticatedUser.userId())
                .stream()
                .map(PaymentResponse::new)
                .toList();

        return ResponseEntity.ok(response);
    }
}
