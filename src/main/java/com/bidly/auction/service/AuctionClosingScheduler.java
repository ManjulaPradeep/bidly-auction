package com.bidly.auction.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AuctionClosingScheduler {

    private final ItemService itemService;

    public AuctionClosingScheduler(ItemService itemService) {
        this.itemService = itemService;
    }

    @Scheduled(fixedDelayString = "${auction.close.delay-ms:15000}")
    public void closeExpiredAuctions() {
        itemService.closeExpiredAuctions();
    }
}
