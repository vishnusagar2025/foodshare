package com.example.foodshare.controller;
import com.example.foodshare.dto.ListingRequest;
import com.example.foodshare.entity.FoodListing;
import com.example.foodshare.service.ListingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/listings")
@CrossOrigin
public class ListingController {
    @Autowired ListingService service;

    /** e.g. GET /api/listings?status=AVAILABLE&page=0&size=10&sort=safeUntil,asc */
    @GetMapping
    public Page<FoodListing> search(@RequestParam(required = false) FoodListing.Status status,
                                    @PageableDefault(size = 10, sort = "safeUntil") Pageable pageable) {
        return service.search(status, pageable);
    }

    @GetMapping("/{id}") public FoodListing one(@PathVariable Long id) { return service.get(id); }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public FoodListing create(@Valid @RequestBody ListingRequest r) { return service.create(r); }

    @PutMapping("/{id}") public FoodListing update(@PathVariable Long id, @Valid @RequestBody ListingRequest r) { return service.update(id, r); }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
