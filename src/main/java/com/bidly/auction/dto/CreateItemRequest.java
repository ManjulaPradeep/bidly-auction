package com.bidly.auction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateItemRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 3000) String description,
        @NotNull @DecimalMin(value = "0.01") BigDecimal startingPrice,
        @NotNull @Future LocalDateTime auctionEndTime
) {
}
