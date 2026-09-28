package com.example.foodshare.service;
import com.example.foodshare.entity.*;
import com.example.foodshare.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.YearMonth;
import java.util.*;

@Service
public class ReportService {
    @Autowired ClaimRepository claims;
    @Autowired FoodListingRepository listings;

    /** Food diverted from waste (collected) versus wasted (expired), per month. */
    public List<Map<String, Object>> monthly() {
        Map<String, Map<String, Object>> out = new TreeMap<>();
        for (Claim c : claims.findByStatus(Claim.Status.COLLECTED)) add(out, YearMonth.from(c.collectedAt).toString(), c.listing, true);
        for (FoodListing f : listings.findByStatus(FoodListing.Status.EXPIRED)) add(out, YearMonth.from(f.safeUntil).toString(), f, false);
        return new ArrayList<>(out.values());
    }

    private void add(Map<String, Map<String, Object>> out, String month, FoodListing f, boolean diverted) {
        Map<String, Object> m = out.computeIfAbsent(month, k -> {
            Map<String, Object> x = new LinkedHashMap<>();
            x.put("month", k); x.put("divertedListings", 0); x.put("divertedQuantity", 0); x.put("divertedWeightKg", 0.0);
            x.put("expiredListings", 0); x.put("expiredWeightKg", 0.0);
            return x;
        });
        String p = diverted ? "diverted" : "expired";
        m.merge(p + "Listings", 1, (a, b) -> (Integer) a + 1);
        if (diverted) m.merge("divertedQuantity", f.quantity, (a, b) -> (Integer) a + (Integer) b);
        m.merge(p + "WeightKg", f.weightKg, (a, b) -> Math.round(((Double) a + (Double) b) * 100) / 100.0);
    }
}
