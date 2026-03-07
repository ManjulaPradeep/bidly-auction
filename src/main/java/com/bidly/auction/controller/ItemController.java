package com.bidly.auction.controller;

import com.bidly.auction.dto.CreateItemRequest;
import com.bidly.auction.dto.ItemResponse;
import com.bidly.auction.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/items")
@Tag(name = "Items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
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
    public List<ItemResponse> listActiveItems(
            @RequestHeader(name = "X-User-Email", required = false) String requesterEmail
    ) {
        return itemService.listActiveItems(requesterEmail);
    }

    @GetMapping("/{itemId}")
    @Operation(summary = "Get item details (BIDDER/ADMIN)")
    public ItemResponse getItem(
            @PathVariable Long itemId,
            @RequestHeader(name = "X-User-Email", required = false) String requesterEmail
    ) {
        return itemService.getItem(itemId, requesterEmail);
    }
}
