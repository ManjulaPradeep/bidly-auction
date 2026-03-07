package com.bidly.auction.service;

import com.bidly.auction.domain.Item;
import com.bidly.auction.domain.ItemStatus;
import com.bidly.auction.domain.User;
import com.bidly.auction.dto.CreateItemRequest;
import com.bidly.auction.dto.ItemResponse;
import com.bidly.auction.dto.ItemSearchFilter;
import com.bidly.auction.dto.WinnerResponse;
import com.bidly.auction.repository.BidRepository;
import com.bidly.auction.repository.ItemRepository;
import com.bidly.auction.repository.ItemSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final BidRepository bidRepository;
    private final CurrentUserService currentUserService;

    public ItemService(ItemRepository itemRepository, BidRepository bidRepository, CurrentUserService currentUserService) {
        this.itemRepository = itemRepository;
        this.bidRepository = bidRepository;
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
    public Page<ItemResponse> listActiveItems(String requesterEmail, Pageable pageable) {
        return listItems(new ItemSearchFilter(null, ItemStatus.ACTIVE, null, null, null, null), requesterEmail, pageable);
    }

    @Transactional
    public Page<ItemResponse> listItems(ItemSearchFilter filter, String requesterEmail, Pageable pageable) {
        User requester = currentUserService.requireUser(requesterEmail);
        requireRole(requester, "BIDDER", "ADMIN");

        closeExpiredAuctions();

        validateRange(filter.minStartingPrice(), filter.maxStartingPrice(), "startingPrice");
        validateDateRange(filter.auctionEndAfter(), filter.auctionEndBefore(), "auctionEndTime");

        ItemStatus status = filter.status() == null ? ItemStatus.ACTIVE : filter.status();
        Specification<Item> spec = Specification
                .where(ItemSpecifications.hasStatus(status))
                .and(ItemSpecifications.keywordInTitleOrDescription(filter.keyword()))
                .and(ItemSpecifications.startingPriceGte(filter.minStartingPrice()))
                .and(ItemSpecifications.startingPriceLte(filter.maxStartingPrice()))
                .and(ItemSpecifications.auctionEndAfter(filter.auctionEndAfter()))
                .and(ItemSpecifications.auctionEndBefore(filter.auctionEndBefore()));

        return itemRepository.findAll(spec, pageable).map(ItemResponse::from);
    }

    @Transactional
    public ItemResponse getItem(Long itemId, String requesterEmail) {
        User requester = currentUserService.requireUser(requesterEmail);
        requireRole(requester, "BIDDER", "ADMIN");

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        closeIfExpired(item);

        return ItemResponse.from(item);
    }

    @Transactional
    public WinnerResponse getWinner(Long itemId, String requesterEmail) {
        User requester = currentUserService.requireUser(requesterEmail);
        requireRole(requester, "BIDDER", "ADMIN");

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        closeIfExpired(item);

        if (item.getStatus() != ItemStatus.CLOSED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Auction is still active");
        }

        var winnerBid = bidRepository.findTopByItemIdOrderByBidAmountDescCreatedAtAscIdAsc(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No winning bid for this item"));

        item.setWinnerBidId(winnerBid.getId());
        return WinnerResponse.from(itemId, winnerBid);
    }

    @Transactional
    public int closeExpiredAuctions() {
        List<Item> expiredItems = itemRepository.findByStatusAndAuctionEndTimeLessThanEqual(ItemStatus.ACTIVE, LocalDateTime.now());
        for (Item item : expiredItems) {
            item.setStatus(ItemStatus.CLOSED);
        }
        return expiredItems.size();
    }

    private void closeIfExpired(Item item) {
        if (item.getStatus() == ItemStatus.ACTIVE && !item.getAuctionEndTime().isAfter(LocalDateTime.now())) {
            item.setStatus(ItemStatus.CLOSED);
        }
    }

    private void validateRange(BigDecimal min, BigDecimal max, String field) {
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " min must be <= max");
        }
    }

    private void validateDateRange(LocalDateTime after, LocalDateTime before, String field) {
        if (after != null && before != null && after.isAfter(before)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " after must be <= before");
        }
    }

    private void requireRole(User user, String... acceptedRoles) {
        String roleName = user.getRole().getName();
        boolean allowed = Arrays.stream(acceptedRoles).anyMatch(role -> role.equalsIgnoreCase(roleName));
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied for role: " + roleName);
        }
    }
}
