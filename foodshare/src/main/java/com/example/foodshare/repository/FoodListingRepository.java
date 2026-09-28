package com.example.foodshare.repository;
import com.example.foodshare.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.*;
import java.time.*;
import java.util.*;
public interface FoodListingRepository extends JpaRepository<FoodListing, Long> { Page<FoodListing> findByStatus(FoodListing.Status s, Pageable p); List<FoodListing> findByStatus(FoodListing.Status s); List<FoodListing> findByStatusAndSafeUntilBefore(FoodListing.Status s, LocalDateTime t);}
