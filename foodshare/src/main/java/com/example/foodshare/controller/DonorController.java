package com.example.foodshare.controller;
import com.example.foodshare.entity.Donor;
import com.example.foodshare.exception.NotFoundException;
import com.example.foodshare.repository.DonorRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/donors")
@CrossOrigin
public class DonorController {
    @Autowired DonorRepository repo;

    @GetMapping public List<Donor> all() { return repo.findAll(); }

    @GetMapping("/{id}") public Donor one(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Donor " + id + " not found"));
    }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Donor create(@Valid @RequestBody Donor x) { x.id = null; return repo.save(x); }

    @PutMapping("/{id}") public Donor update(@PathVariable Long id, @Valid @RequestBody Donor x) {
        one(id); x.id = id; return repo.save(x);
    }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { one(id); repo.deleteById(id); }
}
