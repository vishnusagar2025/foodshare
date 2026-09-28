package com.example.foodshare.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
@Entity
public class Claim {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  @ManyToOne(optional=false) public FoodListing listing; @ManyToOne(optional=false) public Ngo ngo; @Enumerated(EnumType.STRING) public Status status=Status.ACTIVE; public LocalDateTime claimedAt=LocalDateTime.now(); public LocalDateTime collectedAt; public enum Status { ACTIVE, COLLECTED, CANCELLED }
}
