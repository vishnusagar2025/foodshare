package com.example.foodshare.repository;
import com.example.foodshare.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.*;
import java.time.*;
import java.util.*;
public interface NgoRepository extends JpaRepository<Ngo, Long> {}
