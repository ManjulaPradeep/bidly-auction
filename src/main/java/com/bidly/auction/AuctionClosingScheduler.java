package com.bidly.auction;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AuctionClosingScheduler {
    private final AuctionService auctionService;

    public AuctionClosingScheduler(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @Scheduled(fixedDelayString = "${app.auction.close-check-delay-ms:5000}")
    public void closeExpiredAuctions() {
        auctionService.closeExpiredAuctions();
    }
}

