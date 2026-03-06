package com.bidly.auction;

public record AuthResponse(String accessToken, String tokenType, long expiresInMs, String username, String role) {}

