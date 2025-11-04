package com.finassist.controller;

import com.finassist.model.PortfolioHolding;
import com.finassist.model.PortfolioTransaction;
import com.finassist.service.PortfolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @PostMapping("/holding")
    public ResponseEntity<PortfolioHolding> addHolding(@RequestBody PortfolioHolding holding) {
        PortfolioHolding saved = portfolioService.addOrUpdateHolding(holding);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/transaction")
    public ResponseEntity<PortfolioTransaction> addTransaction(@RequestBody PortfolioTransaction transaction) {
        PortfolioTransaction saved = portfolioService.addTransaction(transaction);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/holdings/{userId}")
    public ResponseEntity<List<PortfolioHolding>> getUserHoldings(@PathVariable Long userId) {
        List<PortfolioHolding> holdings = portfolioService.getUserHoldings(userId);
        return ResponseEntity.ok(holdings);
    }

    @GetMapping("/holdings/{userId}/type/{assetType}")
    public ResponseEntity<List<PortfolioHolding>> getHoldingsByType(
            @PathVariable Long userId, 
            @PathVariable String assetType) {
        List<PortfolioHolding> holdings = portfolioService.getUserHoldingsByType(userId, assetType);
        return ResponseEntity.ok(holdings);
    }

    @GetMapping("/transactions/{userId}")
    public ResponseEntity<List<PortfolioTransaction>> getUserTransactions(@PathVariable Long userId) {
        List<PortfolioTransaction> transactions = portfolioService.getUserTransactions(userId);
        return ResponseEntity.ok(transactions);
    }
}