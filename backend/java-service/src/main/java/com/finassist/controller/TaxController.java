package com.finassist.controller;

import com.finassist.model.TaxCalculation;
import com.finassist.service.TaxService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tax")
public class TaxController {

    private final TaxService taxService;

    public TaxController(TaxService taxService) {
        this.taxService = taxService;
    }

    @PostMapping("/calculate")
    public ResponseEntity<TaxCalculation> calculateTax(@RequestBody TaxCalculation taxCalculation) {
        TaxCalculation result = taxService.calculateTax(taxCalculation);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TaxCalculation>> getUserTaxCalculations(@PathVariable Long userId) {
        List<TaxCalculation> calculations = taxService.getUserTaxCalculations(userId);
        return ResponseEntity.ok(calculations);
    }

    @GetMapping("/user/{userId}/year/{year}")
    public ResponseEntity<TaxCalculation> getTaxByYear(@PathVariable Long userId, @PathVariable String year) {
        TaxCalculation calculation = taxService.getLatestTaxCalculation(userId, year);
        if (calculation != null) {
            return ResponseEntity.ok(calculation);
        }
        return ResponseEntity.notFound().build();
    }
}