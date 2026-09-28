package com.example.foodshare.service;
import com.example.foodshare.entity.*;
import com.example.foodshare.exception.*;
import com.example.foodshare.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClaimService {
    @Autowired FoodListingRepository listings;
    @Autowired NgoRepository ngos;
    @Autowired ClaimRepository claims;
    @Autowired NotificationService notify;

    /** Business rules are checked here, before anything is saved. */
    @Transactional
    public Claim claim(Long listingId, Long ngoId) {
        FoodListing f = listings.findById(listingId).orElseThrow(() -> new NotFoundException("Listing " + listingId + " not found"));
        Ngo n = ngos.findById(ngoId).orElseThrow(() -> new NotFoundException("NGO " + ngoId + " not found"));
        // Rule 1: a listing past its safe-to-eat-until time can no longer be claimed
        if (!f.safeUntil.isAfter(LocalDateTime.now()))
            throw new BusinessRuleException("Listing '" + f.foodName + "' has passed its safe-to-eat-until time and can no longer be claimed");
        if (f.status != FoodListing.Status.AVAILABLE)
            throw new BusinessRuleException("Listing is not available (status: " + f.status + ")");
        // Rule 2: only one NGO can hold an active claim on a listing at a time
        if (claims.existsByListingIdAndStatus(listingId, Claim.Status.ACTIVE))
            throw new BusinessRuleException("Listing already has an active claim from another NGO");
        Claim c = new Claim();
        c.listing = f; c.ngo = n;
        f.status = FoodListing.Status.CLAIMED;
        listings.save(f);
        c = claims.save(c);
        notify.send(f.donor.email, n.name + " claimed your listing '" + f.foodName + "'");
        notify.send(n.email, "You claimed '" + f.foodName + "'. Please collect it before " + f.safeUntil);
        return c;
    }

    private Claim activeClaim(Long id) {
        Claim c = claims.findById(id).orElseThrow(() -> new NotFoundException("Claim " + id + " not found"));
        if (c.status != Claim.Status.ACTIVE) throw new BusinessRuleException("Claim is not active (status: " + c.status + ")");
        return c;
    }

    @Transactional
    public Claim collect(Long id) {
        Claim c = activeClaim(id);
        c.status = Claim.Status.COLLECTED;
        c.collectedAt = LocalDateTime.now();
        c.listing.status = FoodListing.Status.COLLECTED;
        listings.save(c.listing);
        notify.send(c.listing.donor.email, "'" + c.listing.foodName + "' was collected by " + c.ngo.name + ". Thank you!");
        return claims.save(c);
    }

    @Transactional
    public Claim cancel(Long id) {
        Claim c = activeClaim(id);
        c.status = Claim.Status.CANCELLED;
        boolean overdue = !c.listing.safeUntil.isAfter(LocalDateTime.now());
        c.listing.status = overdue ? FoodListing.Status.EXPIRED : FoodListing.Status.AVAILABLE;
        listings.save(c.listing);
        notify.send(c.listing.donor.email, c.ngo.name + " cancelled its claim on '" + c.listing.foodName + "'");
        return claims.save(c);
    }

    public List<Claim> all() { return claims.findAll(); }
}
