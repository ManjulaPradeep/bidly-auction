package com.bidly.auction;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BidRepository extends JpaRepository<Bid, Long> {
    Optional<Bid> findTopByItemIdOrderByAmountDescCreatedAtAsc(Long itemId);
}

