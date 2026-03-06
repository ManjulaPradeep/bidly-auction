package com.bidly.auction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuctionService {
    private final AuctionItemRepository auctionItemRepository;
    private final BidRepository bidRepository;
    private final UserAccountRepository userAccountRepository;

    public AuctionService(
            AuctionItemRepository auctionItemRepository, BidRepository bidRepository, UserAccountRepository userAccountRepository) {
        this.auctionItemRepository = auctionItemRepository;
        this.bidRepository = bidRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional
    public ItemResponse createItem(CreateItemRequest request, String username) {
        UserAccount creator = getUserByUsername(username);

        AuctionItem item = new AuctionItem();
        item.setTitle(request.title().trim());
        item.setDescription(request.description());
        item.setStartingPrice(request.startingPrice());
        item.setCurrentPrice(request.startingPrice());
        item.setAuctionEndTime(request.auctionEndTime());
        item.setStatus(AuctionStatus.OPEN);
        item.setCreatedBy(creator);

        AuctionItem saved = auctionItemRepository.save(item);
        return toItemResponse(saved, null);
    }

    @Transactional(readOnly = true)
    public Page<ItemResponse> getActiveItems(Pageable pageable) {
        return auctionItemRepository
                .findByStatusAndAuctionEndTimeAfter(AuctionStatus.OPEN, LocalDateTime.now(), pageable)
                .map(item -> toItemResponse(item, findHighestBid(item.getId())));
    }

    @Transactional(readOnly = true)
    public ItemResponse getItemById(Long itemId) {
        AuctionItem item = auctionItemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Item not found: " + itemId));
        return toItemResponse(item, findHighestBid(itemId));
    }

    @Transactional
    public BidResponse placeBid(Long itemId, PlaceBidRequest request, String bidderUsername) {
        AuctionItem item = auctionItemRepository
                .findByIdForUpdate(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));

        if (item.getStatus() != AuctionStatus.OPEN || !item.getAuctionEndTime().isAfter(LocalDateTime.now())) {
            closeAuctionInternal(item);
            throw new AuctionClosedException("Auction is closed for item: " + itemId);
        }

        BigDecimal current = item.getCurrentPrice() == null ? item.getStartingPrice() : item.getCurrentPrice();
        BigDecimal minimumAllowed = current.max(item.getStartingPrice());
        if (request.amount().compareTo(minimumAllowed) <= 0) {
            throw new InvalidBidException("Bid must be greater than current price: " + minimumAllowed);
        }

        UserAccount bidder = getUserByUsername(bidderUsername);

        Bid bid = new Bid();
        bid.setItem(item);
        bid.setBidder(bidder);
        bid.setAmount(request.amount());
        Bid savedBid = bidRepository.save(bid);

        item.setCurrentPrice(request.amount());
        auctionItemRepository.save(item);

        return new BidResponse(
                savedBid.getId(), item.getId(), bidder.getUsername(), savedBid.getAmount(), item.getCurrentPrice(), savedBid.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public WinnerResponse getWinner(Long itemId) {
        AuctionItem item = auctionItemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Item not found: " + itemId));
        if (item.getStatus() != AuctionStatus.CLOSED) {
            throw new InvalidBidException("Auction is still open for item: " + itemId);
        }

        Bid winner = item.getWinnerBid();
        return new WinnerResponse(item.getId(), item.getStatus(), toBidSummary(winner));
    }

    @Transactional
    public void closeExpiredAuctions() {
        List<AuctionItem> expiredItems =
                auctionItemRepository.findByStatusAndAuctionEndTimeLessThanEqual(AuctionStatus.OPEN, LocalDateTime.now());
        for (AuctionItem item : expiredItems) {
            closeAuctionInternal(item);
        }
    }

    private void closeAuctionInternal(AuctionItem item) {
        if (item.getStatus() == AuctionStatus.CLOSED) {
            return;
        }
        Bid topBid = findHighestBid(item.getId());
        item.setStatus(AuctionStatus.CLOSED);
        item.setWinnerBid(topBid);
        auctionItemRepository.save(item);
    }

    private UserAccount getUserByUsername(String username) {
        return userAccountRepository
                .findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
    }

    private Bid findHighestBid(Long itemId) {
        return bidRepository.findTopByItemIdOrderByAmountDescCreatedAtAsc(itemId).orElse(null);
    }

    private ItemResponse toItemResponse(AuctionItem item, Bid highestBid) {
        return new ItemResponse(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getStartingPrice(),
                item.getCurrentPrice(),
                item.getAuctionEndTime(),
                item.getStatus(),
                item.getCreatedBy().getUsername(),
                toBidSummary(highestBid));
    }

    private BidSummaryResponse toBidSummary(Bid bid) {
        if (bid == null) {
            return null;
        }
        return new BidSummaryResponse(bid.getId(), bid.getAmount(), bid.getBidder().getUsername(), bid.getCreatedAt());
    }
}
