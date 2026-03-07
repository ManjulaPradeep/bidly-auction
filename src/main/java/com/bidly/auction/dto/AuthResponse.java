package com.bidly.auction.dto;

public record AuthResponse(
        String message,
        String email,
        String name,
        String role,
        String token
) {
}
