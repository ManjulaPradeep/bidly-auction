package com.bidly.auction;

public record WinnerResponse(Long itemId, AuctionStatus status, BidSummaryResponse winningBid) {}

