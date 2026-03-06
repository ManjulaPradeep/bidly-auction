package com.bidly.auction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ItemResponse(
        Long id,
        String title,
        String description,
        BigDecimal startingPrice,
        BigDecimal currentPrice,
        LocalDateTime auctionEndTime,
        AuctionStatus status,
        String createdBy,
        BidSummaryResponse highestBid) {}

