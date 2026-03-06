package com.bidly.auction;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuctionItemRepository extends JpaRepository<AuctionItem, Long> {
    Page<AuctionItem> findByStatusAndAuctionEndTimeAfter(AuctionStatus status, LocalDateTime now, Pageable pageable);

    List<AuctionItem> findByStatusAndAuctionEndTimeLessThanEqual(AuctionStatus status, LocalDateTime now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from AuctionItem i where i.id = :id")
    Optional<AuctionItem> findByIdForUpdate(@Param("id") Long id);
}

