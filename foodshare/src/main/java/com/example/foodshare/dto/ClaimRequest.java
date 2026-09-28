package com.example.foodshare.dto;
import jakarta.validation.constraints.NotNull;
public record ClaimRequest(
    @NotNull(message = "listingId is required") Long listingId,
    @NotNull(message = "ngoId is required") Long ngoId) {}
