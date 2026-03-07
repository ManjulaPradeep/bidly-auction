package com.bidly.auction.dto;

import com.bidly.auction.domain.Bid;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WinnerResponse(
        Long itemId,
        Long winningBidId,
        Long bidderId,
        BigDecimal bidAmount,
        LocalDateTime wonAt
) {
    public static WinnerResponse from(Long itemId, Bid bid) {
        return new WinnerResponse(
                itemId,
                bid.getId(),
                bid.getBidder().getId(),
                bid.getBidAmount(),
                bid.getCreatedAt()
        );
    }
}
