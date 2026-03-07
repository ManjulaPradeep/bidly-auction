package com.bidly.auction.repository;

import com.bidly.auction.domain.Item;
import com.bidly.auction.domain.ItemStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class ItemSpecifications {

    private ItemSpecifications() {
    }

    public static Specification<Item> hasStatus(ItemStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Item> keywordInTitleOrDescription(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }
            String value = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), value),
                    cb.like(cb.lower(root.get("description")), value)
            );
        };
    }

    public static Specification<Item> startingPriceGte(BigDecimal minPrice) {
        return (root, query, cb) -> minPrice == null ? null : cb.greaterThanOrEqualTo(root.get("startingPrice"), minPrice);
    }

    public static Specification<Item> startingPriceLte(BigDecimal maxPrice) {
        return (root, query, cb) -> maxPrice == null ? null : cb.lessThanOrEqualTo(root.get("startingPrice"), maxPrice);
    }

    public static Specification<Item> auctionEndAfter(LocalDateTime endAfter) {
        return (root, query, cb) -> endAfter == null ? null : cb.greaterThanOrEqualTo(root.get("auctionEndTime"), endAfter);
    }

    public static Specification<Item> auctionEndBefore(LocalDateTime endBefore) {
        return (root, query, cb) -> endBefore == null ? null : cb.lessThanOrEqualTo(root.get("auctionEndTime"), endBefore);
    }
}
