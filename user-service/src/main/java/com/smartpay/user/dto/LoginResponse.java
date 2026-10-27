package com.smartpay.user.dto;
public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
