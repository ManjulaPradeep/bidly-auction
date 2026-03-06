package com.bidly.auction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateItemRequest(
        @NotBlank String title,
        String description,
        @NotNull @DecimalMin(value = "0.01") BigDecimal startingPrice,
        @NotNull @Future LocalDateTime auctionEndTime) {}

