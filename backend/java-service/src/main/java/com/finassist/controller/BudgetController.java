package com.finassist.controller;

import com.finassist.model.BudgetEntry;
import com.finassist.service.BudgetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budget")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping("/entry")
    public ResponseEntity<BudgetEntry> addEntry(@RequestBody BudgetEntry entry) {
        BudgetEntry savedEntry = budgetService.addEntry(entry);
        return ResponseEntity.ok(savedEntry);
    }

    @GetMapping("/entries/{userId}")
    public ResponseEntity<List<BudgetEntry>> getUserEntries(@PathVariable Long userId) {
        List<BudgetEntry> entries = budgetService.getUserEntries(userId);
        return ResponseEntity.ok(entries);
    }

    @DeleteMapping("/entry/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable Long id) {
        budgetService.deleteEntry(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/entries/{userId}/{type}")
    public ResponseEntity<List<BudgetEntry>> getUserEntriesByType(
            @PathVariable Long userId, 
            @PathVariable String type) {
        List<BudgetEntry> entries = budgetService.getUserEntriesByType(userId, type);
        return ResponseEntity.ok(entries);
    }
}