package com.bidly.auction.dto;

import java.math.BigDecimal;

public class BidUpdateMessage {

    private Long itemId;
    private BigDecimal newHighestBid;
    private String bidder;

    public BidUpdateMessage(Long itemId, BigDecimal newHighestBid, String bidder) {
        this.itemId = itemId;
        this.newHighestBid = newHighestBid;
        this.bidder = bidder;
    }

    public Long getItemId() {
        return itemId;
    }

    public BigDecimal getNewHighestBid() {
        return newHighestBid;
    }

    public String getBidder() {
        return bidder;
    }
}
