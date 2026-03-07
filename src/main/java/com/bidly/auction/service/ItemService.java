package com.bidly.auction.service;

import com.bidly.auction.domain.Item;
import com.bidly.auction.domain.ItemStatus;
import com.bidly.auction.domain.User;
import com.bidly.auction.dto.CreateItemRequest;
import com.bidly.auction.dto.ItemResponse;
import com.bidly.auction.repository.ItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final CurrentUserService currentUserService;

    public ItemService(ItemRepository itemRepository, CurrentUserService currentUserService) {
        this.itemRepository = itemRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public ItemResponse createItem(CreateItemRequest request, String requesterEmail) {
        User requester = currentUserService.requireUser(requesterEmail);
        requireRole(requester, "ADMIN");

        Item item = new Item();
        item.setTitle(request.title().trim());
        item.setDescription(request.description() == null ? null : request.description().trim());
        item.setStartingPrice(request.startingPrice());
        item.setCurrentHighestBid(request.startingPrice());
        item.setAuctionEndTime(request.auctionEndTime());
        item.setStatus(ItemStatus.ACTIVE);
        item.setCreatedBy(requester);

        return ItemResponse.from(itemRepository.save(item));
    }

    @Transactional
    public List<ItemResponse> listActiveItems(String requesterEmail) {
        User requester = currentUserService.requireUser(requesterEmail);
        requireRole(requester, "BIDDER", "ADMIN");

        closeExpiredAuctions();

        return itemRepository.findByStatusAndAuctionEndTimeAfterOrderByAuctionEndTimeAsc(ItemStatus.ACTIVE, LocalDateTime.now())
                .stream()
                .map(ItemResponse::from)
                .toList();
    }

    @Transactional
    public ItemResponse getItem(Long itemId, String requesterEmail) {
        User requester = currentUserService.requireUser(requesterEmail);
        requireRole(requester, "BIDDER", "ADMIN");

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        if (item.getStatus() == ItemStatus.ACTIVE && item.getAuctionEndTime().isBefore(LocalDateTime.now())) {
            item.setStatus(ItemStatus.CLOSED);
        }

        return ItemResponse.from(item);
    }

    @Transactional
    public int closeExpiredAuctions() {
        List<Item> expiredItems = itemRepository.findByStatusAndAuctionEndTimeLessThanEqual(ItemStatus.ACTIVE, LocalDateTime.now());
        for (Item item : expiredItems) {
            item.setStatus(ItemStatus.CLOSED);
        }
        return expiredItems.size();
    }

    private void requireRole(User user, String... acceptedRoles) {
        String roleName = user.getRole().getName();
        boolean allowed = Arrays.stream(acceptedRoles).anyMatch(role -> role.equalsIgnoreCase(roleName));
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied for role: " + roleName);
        }
    }
}
