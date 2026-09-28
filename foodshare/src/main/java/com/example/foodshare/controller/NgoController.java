package com.example.foodshare.controller;
import com.example.foodshare.entity.Ngo;
import com.example.foodshare.exception.NotFoundException;
import com.example.foodshare.repository.NgoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ngos")
@CrossOrigin
public class NgoController {
    @Autowired NgoRepository repo;

    @GetMapping public List<Ngo> all() { return repo.findAll(); }

    @GetMapping("/{id}") public Ngo one(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Ngo " + id + " not found"));
    }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Ngo create(@Valid @RequestBody Ngo x) { x.id = null; return repo.save(x); }

    @PutMapping("/{id}") public Ngo update(@PathVariable Long id, @Valid @RequestBody Ngo x) {
        one(id); x.id = id; return repo.save(x);
    }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { one(id); repo.deleteById(id); }
}
