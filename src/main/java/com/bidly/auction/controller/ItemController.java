package com.bidly.auction.controller;

import com.bidly.auction.dto.CreateItemRequest;
import com.bidly.auction.dto.BidResponse;
import com.bidly.auction.dto.ItemResponse;
import com.bidly.auction.dto.ItemSearchFilter;
import com.bidly.auction.dto.PlaceBidRequest;
import com.bidly.auction.dto.WinnerResponse;
import com.bidly.auction.domain.ItemStatus;
import com.bidly.auction.service.BidService;
import com.bidly.auction.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/items")
@Tag(name = "Items")
public class ItemController {

    private final ItemService itemService;
    private final BidService bidService;

    public ItemController(ItemService itemService, BidService bidService) {
        this.itemService = itemService;
        this.bidService = bidService;
    }

    @PostMapping
    @Operation(summary = "Create a new item (ADMIN only)")
    public ItemResponse createItem(
            @Valid @RequestBody CreateItemRequest request,
            @RequestHeader(name = "X-User-Email", required = false) String requesterEmail
    ) {
        return itemService.createItem(request, requesterEmail);
    }

    @GetMapping
    @Operation(summary = "List active auction items (BIDDER/ADMIN)")
    public Page<ItemResponse> listActiveItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "auctionEndTime") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestHeader(name = "X-User-Email", required = false) String requesterEmail
    ) {
        return itemService.listActiveItems(requesterEmail, buildPageable(page, size, sortBy, sortDir));
    }

    @GetMapping("/search")
    @Operation(summary = "Search/filter auction items (BIDDER/ADMIN)")
    public Page<ItemResponse> searchItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ItemStatus status,
            @RequestParam(required = false) BigDecimal minStartingPrice,
            @RequestParam(required = false) BigDecimal maxStartingPrice,
            @RequestParam(required = false) LocalDateTime auctionEndAfter,
            @RequestParam(required = false) LocalDateTime auctionEndBefore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "auctionEndTime") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestHeader(name = "X-User-Email", required = false) String requesterEmail
    ) {
        ItemSearchFilter filter = new ItemSearchFilter(
                keyword,
                status,
                minStartingPrice,
                maxStartingPrice,
                auctionEndAfter,
                auctionEndBefore
        );
        return itemService.listItems(filter, requesterEmail, buildPageable(page, size, sortBy, sortDir));
    }

    @GetMapping("/{itemId}")
    @Operation(summary = "Get item details (BIDDER/ADMIN)")
    public ItemResponse getItem(
            @PathVariable Long itemId,
            @RequestHeader(name = "X-User-Email", required = false) String requesterEmail
    ) {
        return itemService.getItem(itemId, requesterEmail);
    }

    @PostMapping("/{itemId}/bids")
    @Operation(summary = "Place a bid for an item (BIDDER only)")
    public BidResponse placeBid(
            @PathVariable Long itemId,
            @Valid @RequestBody PlaceBidRequest request,
            @RequestHeader(name = "X-User-Email", required = false) String requesterEmail
    ) {
        return bidService.placeBid(itemId, request, requesterEmail);
    }

    @GetMapping("/{itemId}/winner")
    @Operation(summary = "Get winner details for a closed auction item")
    public WinnerResponse getWinner(
            @PathVariable Long itemId,
            @RequestHeader(name = "X-User-Email", required = false) String requesterEmail
    ) {
        return itemService.getWinner(itemId, requesterEmail);
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? "auctionEndTime" : sortBy.trim();
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, 100));
        return PageRequest.of(safePage, safeSize, Sort.by(direction, safeSortBy));
    }
}
