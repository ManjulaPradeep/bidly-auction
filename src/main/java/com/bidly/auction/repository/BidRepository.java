package com.bidly.auction.repository;

import com.bidly.auction.domain.Bid;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {
    Optional<Bid> findTopByItemIdOrderByBidAmountDescCreatedAtAscIdAsc(Long itemId);
}
