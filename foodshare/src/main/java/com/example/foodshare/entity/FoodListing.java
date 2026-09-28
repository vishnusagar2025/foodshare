package com.example.foodshare.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
@Entity
public class FoodListing {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  @ManyToOne(optional=false) public Donor donor; public String foodName; public String foodType; public int quantity; public double weightKg; public LocalDateTime safeUntil; @Enumerated(EnumType.STRING) public Status status=Status.AVAILABLE; public LocalDateTime createdAt=LocalDateTime.now(); public enum Status { AVAILABLE, CLAIMED, COLLECTED, EXPIRED }
}
