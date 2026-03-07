package com.bidly.auction.dto;

import com.bidly.auction.domain.Bid;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BidResponse(
        Long bidId,
        Long itemId,
        Long bidderId,
        BigDecimal bidAmount,
        BigDecimal currentHighestBid,
        LocalDateTime createdAt
) {
    public static BidResponse from(Bid bid, BigDecimal currentHighestBid) {
        return new BidResponse(
                bid.getId(),
                bid.getItem().getId(),
                bid.getBidder().getId(),
                bid.getBidAmount(),
                currentHighestBid,
                bid.getCreatedAt()
        );
    }
}
