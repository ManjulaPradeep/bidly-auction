package com.bidly.auction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BidResponse(
        Long bidId, Long itemId, String bidderUsername, BigDecimal amount, BigDecimal currentPrice, LocalDateTime placedAt) {}

