package com.example.foodshare.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
public record ListingRequest(
    @NotNull(message = "donorId is required") Long donorId,
    @NotBlank(message = "foodName is required") String foodName,
    @NotBlank(message = "foodType is required") String foodType,
    @NotNull(message = "quantity is required") @Positive(message = "quantity must be positive") Integer quantity,
    @NotNull(message = "weightKg is required") @Positive(message = "weightKg must be positive") Double weightKg,
    @NotNull(message = "safeUntil is required") LocalDateTime safeUntil) {}
