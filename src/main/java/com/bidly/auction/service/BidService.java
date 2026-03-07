package com.bidly.auction.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.bidly.auction.domain.Bid;
import com.bidly.auction.domain.Item;
import com.bidly.auction.domain.ItemStatus;
import com.bidly.auction.domain.User;
import com.bidly.auction.dto.BidResponse;
import com.bidly.auction.dto.PlaceBidRequest;
import com.bidly.auction.repository.BidRepository;
import com.bidly.auction.repository.ItemRepository;


@Service
public class BidService {

    private final BidRepository bidRepository;
    private final ItemRepository itemRepository;
    private final CurrentUserService currentUserService;
    private final SimpMessagingTemplate messagingTemplate;

    public BidService(BidRepository bidRepository, ItemRepository itemRepository, CurrentUserService currentUserService, SimpMessagingTemplate messagingTemplate) {
        this.bidRepository = bidRepository;
        this.itemRepository = itemRepository;
        this.currentUserService = currentUserService;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public BidResponse placeBid(Long itemId, PlaceBidRequest request, String requesterEmail) {
        User bidder = currentUserService.requireUser(requesterEmail);
        requireRole(bidder, "BIDDER");

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        if (item.getStatus() == ItemStatus.CLOSED || !item.getAuctionEndTime().isAfter(LocalDateTime.now())) {
            item.setStatus(ItemStatus.CLOSED);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Auction is closed for this item");
        }

        BigDecimal currentHighestBid = item.getCurrentHighestBid() == null
                ? item.getStartingPrice()
                : item.getCurrentHighestBid();

        if (request.bidAmount().compareTo(currentHighestBid) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Bid amount must be greater than current highest bid: " + currentHighestBid
            );
        }

        Bid bid = new Bid();
        bid.setItem(item);
        bid.setBidder(bidder);
        bid.setBidAmount(request.bidAmount());

        item.setCurrentHighestBid(request.bidAmount());

        Bid savedBid = bidRepository.save(bid);
        BidResponse response = BidResponse.from(savedBid, item.getCurrentHighestBid());

        messagingTemplate.convertAndSend("/topic/items/" + itemId, response);
        return response;
    }

    private void requireRole(User user, String... acceptedRoles) {
        String roleName = user.getRole().getName();
        boolean allowed = Arrays.stream(acceptedRoles).anyMatch(role -> role.equalsIgnoreCase(roleName));
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied for role: " + roleName);
        }
    }
}
