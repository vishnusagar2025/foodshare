package com.example.foodshare.controller;
import com.example.foodshare.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin
public class ReportController {
    @Autowired ReportService service;
    @GetMapping("/diverted-monthly") public List<Map<String, Object>> monthly() { return service.monthly(); }
}
