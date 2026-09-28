package com.example.foodshare.controller;
import com.example.foodshare.dto.ClaimRequest;
import com.example.foodshare.entity.Claim;
import com.example.foodshare.service.ClaimService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/claims")
@CrossOrigin
public class ClaimController {
    @Autowired ClaimService service;

    @GetMapping public List<Claim> all() { return service.all(); }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Claim claim(@Valid @RequestBody ClaimRequest r) { return service.claim(r.listingId(), r.ngoId()); }

    @PutMapping("/{id}/collect") public Claim collect(@PathVariable Long id) { return service.collect(id); }

    @PutMapping("/{id}/cancel") public Claim cancel(@PathVariable Long id) { return service.cancel(id); }
}
