package com.example.foodshare.service;
import com.example.foodshare.dto.ListingRequest;
import com.example.foodshare.entity.*;
import com.example.foodshare.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {
    @Autowired DonorRepository donors;
    @Autowired NgoRepository ngos;
    @Autowired ListingService listings;

    @Override
    public void run(String... args) {
        if (donors.count() > 0) return;
        Donor d1 = new Donor(); d1.name = "Main Canteen"; d1.email = "canteen@campus.example"; d1.phone = "9000000001"; d1.address = "Block A"; donors.save(d1);
        Donor d2 = new Donor(); d2.name = "Green Leaf Caterers"; d2.email = "events@greenleaf.example"; d2.phone = "9000000002"; d2.address = "Market Road"; donors.save(d2);
        Ngo n1 = new Ngo(); n1.name = "Hope Shelter"; n1.email = "help@hope.example"; n1.phone = "9000000003"; n1.address = "North Street"; ngos.save(n1);
        Ngo n2 = new Ngo(); n2.name = "Meals For All"; n2.email = "team@mealsforall.example"; n2.phone = "9000000004"; n2.address = "Station Road"; ngos.save(n2);
        LocalDateTime now = LocalDateTime.now();
        listings.create(new ListingRequest(d1.id, "Vegetable biryani", "Cooked meal", 40, 12.5, now.plusHours(3)));
        listings.create(new ListingRequest(d2.id, "Bread loaves", "Bakery", 30, 9.0, now.plusHours(20)));
        listings.create(new ListingRequest(d1.id, "Fruit boxes", "Produce", 15, 6.0, now.plusDays(2)));
    }
}
