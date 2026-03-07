package com.bidly.auction.dto;

import com.bidly.auction.domain.ItemStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ItemSearchFilter(
        String keyword,
        ItemStatus status,
        BigDecimal minStartingPrice,
        BigDecimal maxStartingPrice,
        LocalDateTime auctionEndAfter,
        LocalDateTime auctionEndBefore
) {
}
