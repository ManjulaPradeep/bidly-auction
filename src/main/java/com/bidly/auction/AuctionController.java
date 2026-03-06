package com.bidly.auction;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/items")
public class AuctionController {
    private final AuctionService auctionService;

    public AuctionController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @PostMapping
    public ItemResponse createItem(@Valid @RequestBody CreateItemRequest request, Authentication authentication) {
        return auctionService.createItem(request, authentication.getName());
    }

    @GetMapping
    public Page<ItemResponse> getActiveItems(@PageableDefault(size = 20) Pageable pageable) {
        return auctionService.getActiveItems(pageable);
    }

    @GetMapping("/{itemId}")
    public ItemResponse getItemById(@PathVariable Long itemId) {
        return auctionService.getItemById(itemId);
    }

    @PostMapping("/{itemId}/bids")
    public BidResponse placeBid(
            @PathVariable Long itemId, @Valid @RequestBody PlaceBidRequest request, Authentication authentication) {
        return auctionService.placeBid(itemId, request, authentication.getName());
    }

    @GetMapping("/{itemId}/winner")
    public WinnerResponse getWinner(@PathVariable Long itemId) {
        return auctionService.getWinner(itemId);
    }
}

