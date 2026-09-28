package com.example.foodshare.service;
import com.example.foodshare.dto.ListingRequest;
import com.example.foodshare.entity.*;
import com.example.foodshare.exception.*;
import com.example.foodshare.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ListingService {
    @Autowired FoodListingRepository listings;
    @Autowired DonorRepository donors;
    @Autowired ClaimRepository claims;
    @Autowired NotificationService notify;

    private void checkFuture(LocalDateTime t) {
        if (!t.isAfter(LocalDateTime.now()))
            throw new BusinessRuleException("Safe-to-eat-until time must be in the future");
    }

    private void apply(FoodListing f, ListingRequest r, Donor d) {
        f.donor = d; f.foodName = r.foodName(); f.foodType = r.foodType();
        f.quantity = r.quantity(); f.weightKg = r.weightKg(); f.safeUntil = r.safeUntil();
    }

    @Transactional
    public FoodListing create(ListingRequest r) {
        Donor d = donors.findById(r.donorId()).orElseThrow(() -> new NotFoundException("Donor " + r.donorId() + " not found"));
        checkFuture(r.safeUntil());
        FoodListing f = new FoodListing();
        apply(f, r, d);
        f = listings.save(f);
        notify.send(d.email, "Your listing '" + f.foodName + "' is now live until " + f.safeUntil);
        return f;
    }

    @Transactional
    public FoodListing get(Long id) {
        FoodListing f = listings.findById(id).orElseThrow(() -> new NotFoundException("Listing " + id + " not found"));
        expireIfOverdue(f);
        return f;
    }

    @Transactional
    public FoodListing update(Long id, ListingRequest r) {
        FoodListing f = get(id);
        if (f.status != FoodListing.Status.AVAILABLE)
            throw new BusinessRuleException("Only AVAILABLE listings can be edited (this one is " + f.status + ")");
        checkFuture(r.safeUntil());
        Donor d = donors.findById(r.donorId()).orElseThrow(() -> new NotFoundException("Donor " + r.donorId() + " not found"));
        apply(f, r, d);
        return listings.save(f);
    }

    @Transactional
    public void delete(Long id) {
        FoodListing f = get(id);
        if (f.status == FoodListing.Status.CLAIMED) throw new BusinessRuleException("Cannot delete a listing that has an active claim");
        if (claims.existsByListingId(id)) throw new BusinessRuleException("Cannot delete a listing that has claim history");
        listings.delete(f);
    }

    @Transactional
    public Page<FoodListing> search(FoodListing.Status status, Pageable p) {
        expireOverdue();
        return status == null ? listings.findAll(p) : listings.findByStatus(status, p);
    }

    private void expireIfOverdue(FoodListing f) {
        if (f.status == FoodListing.Status.AVAILABLE && !f.safeUntil.isAfter(LocalDateTime.now())) {
            f.status = FoodListing.Status.EXPIRED;
            listings.save(f);
            notify.send(f.donor.email, "Your listing '" + f.foodName + "' expired unclaimed");
        }
    }

    /** Auto-expire: runs every 30 seconds and marks unclaimed overdue listings EXPIRED. */
    @Scheduled(fixedRate = 30000)
    @Transactional
    public int expireOverdue() {
        List<FoodListing> overdue = listings.findByStatusAndSafeUntilBefore(FoodListing.Status.AVAILABLE, LocalDateTime.now());
        overdue.forEach(this::expireIfOverdue);
        return overdue.size();
    }
}
