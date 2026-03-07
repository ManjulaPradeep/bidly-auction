package com.bidly.auction.repository;

import com.bidly.auction.domain.Item;
import com.bidly.auction.domain.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByStatusAndAuctionEndTimeAfterOrderByAuctionEndTimeAsc(ItemStatus status, LocalDateTime dateTime);
    List<Item> findByStatusAndAuctionEndTimeBefore(ItemStatus status, LocalDateTime dateTime);
    List<Item> findByStatusAndAuctionEndTimeLessThanEqual(ItemStatus status, LocalDateTime dateTime);
}
