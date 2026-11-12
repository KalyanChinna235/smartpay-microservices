package com.smartpay.payment.security;

public record AuthenticatedUser(Long userId, String email) {
}