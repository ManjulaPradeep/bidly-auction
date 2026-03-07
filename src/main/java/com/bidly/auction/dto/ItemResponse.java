package com.bidly.auction.dto;

import com.bidly.auction.domain.Item;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ItemResponse(
        Long id,
        String title,
        String description,
        BigDecimal startingPrice,
        BigDecimal currentHighestBid,
        LocalDateTime auctionEndTime,
        String status,
        Long createdByUserId,
        LocalDateTime createdAt
) {
    public static ItemResponse from(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getStartingPrice(),
                item.getCurrentHighestBid(),
                item.getAuctionEndTime(),
                item.getStatus().name(),
                item.getCreatedBy().getId(),
                item.getCreatedAt()
        );
    }
}
