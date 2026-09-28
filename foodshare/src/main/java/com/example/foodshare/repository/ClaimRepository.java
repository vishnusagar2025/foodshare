package com.example.foodshare.repository;
import com.example.foodshare.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.*;
import java.time.*;
import java.util.*;
public interface ClaimRepository extends JpaRepository<Claim, Long> { boolean existsByListingIdAndStatus(Long listingId, Claim.Status s); boolean existsByListingId(Long listingId); List<Claim> findByStatus(Claim.Status s);}
