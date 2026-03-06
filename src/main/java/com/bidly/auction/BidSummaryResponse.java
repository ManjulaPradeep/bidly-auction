package com.bidly.auction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BidSummaryResponse(Long bidId, BigDecimal amount, String bidderUsername, LocalDateTime placedAt) {}

