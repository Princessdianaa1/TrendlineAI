package com.finassist.service;

import com.finassist.model.TaxCalculation;
import com.finassist.repository.TaxRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class TaxService {

    private final TaxRepository taxRepository;

    public TaxService(TaxRepository taxRepository) {
        this.taxRepository = taxRepository;
    }

    public TaxCalculation calculateTax(TaxCalculation tax) {
        // Calculate total income
        BigDecimal totalIncome = tax.getSalaryIncome()
                .add(tax.getHousePropertyIncome())
                .add(tax.getBusinessIncome())
                .add(tax.getCapitalGainsShort())
                .add(tax.getCapitalGainsLong())
                .add(tax.getOtherIncome());
        
        // Calculate total deductions
        BigDecimal totalDeductions = tax.getDeduction80c()
                .add(tax.getDeduction80d())
                .add(tax.getDeduction80ccd1b())
                .add(tax.getDeduction80e())
                .add(tax.getDeduction80g())
                .add(tax.getOtherDeductions());
        
        // Calculate taxable income (for old regime)
        BigDecimal taxableIncome = totalIncome.subtract(totalDeductions);
        if (taxableIncome.compareTo(BigDecimal.ZERO) < 0) {
            taxableIncome = BigDecimal.ZERO;
        }
        
        // Calculate tax under old regime (with deductions)
        BigDecimal taxOldRegime = calculateOldRegimeTax(taxableIncome);
        
        // Calculate tax under new regime (no deductions except standard deduction)
        BigDecimal taxNewRegime = calculateNewRegimeTax(totalIncome);
        
        // Determine recommended regime
        String recommendedRegime = taxOldRegime.compareTo(taxNewRegime) <= 0 ? "Old Regime" : "New Regime";
        
        // Generate tax saving tips
        String taxSavingTips = generateTaxSavingTips(tax, totalDeductions);
        
        // Set calculated values
        tax.setTaxableIncome(taxableIncome);
        tax.setTaxOldRegime(taxOldRegime);
        tax.setTaxNewRegime(taxNewRegime);
        tax.setRecommendedRegime(recommendedRegime);
        tax.setTaxSavingTips(taxSavingTips);
        
        // Save to database
        return taxRepository.save(tax);
    }

    private BigDecimal calculateOldRegimeTax(BigDecimal income) {
        BigDecimal tax = BigDecimal.ZERO;
        
        // Old Regime Tax Slabs (FY 2024-25)
        if (income.compareTo(new BigDecimal("250000")) > 0) {
            BigDecimal slab1 = income.subtract(new BigDecimal("250000")).min(new BigDecimal("250000"));
            tax = tax.add(slab1.multiply(new BigDecimal("0.05")));
        }
        if (income.compareTo(new BigDecimal("500000")) > 0) {
            BigDecimal slab2 = income.subtract(new BigDecimal("500000")).min(new BigDecimal("500000"));
            tax = tax.add(slab2.multiply(new BigDecimal("0.20")));
        }
        if (income.compareTo(new BigDecimal("1000000")) > 0) {
            BigDecimal slab3 = income.subtract(new BigDecimal("1000000"));
            tax = tax.add(slab3.multiply(new BigDecimal("0.30")));
        }
        
        // Add 4% cess
        tax = tax.multiply(new BigDecimal("1.04"));
        
        return tax.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateNewRegimeTax(BigDecimal income) {
        BigDecimal tax = BigDecimal.ZERO;
        
        // New Regime Tax Slabs (FY 2024-25)
        if (income.compareTo(new BigDecimal("300000")) > 0) {
            BigDecimal slab1 = income.subtract(new BigDecimal("300000")).min(new BigDecimal("300000"));
            tax = tax.add(slab1.multiply(new BigDecimal("0.05")));
        }
        if (income.compareTo(new BigDecimal("600000")) > 0) {
            BigDecimal slab2 = income.subtract(new BigDecimal("600000")).min(new BigDecimal("100000"));
            tax = tax.add(slab2.multiply(new BigDecimal("0.10")));
        }
        if (income.compareTo(new BigDecimal("900000")) > 0) {
            BigDecimal slab3 = income.subtract(new BigDecimal("900000")).min(new BigDecimal("300000"));
            tax = tax.add(slab3.multiply(new BigDecimal("0.15")));
        }
        if (income.compareTo(new BigDecimal("1200000")) > 0) {
            BigDecimal slab4 = income.subtract(new BigDecimal("1200000")).min(new BigDecimal("300000"));
            tax = tax.add(slab4.multiply(new BigDecimal("0.20")));
        }
        if (income.compareTo(new BigDecimal("1500000")) > 0) {
            BigDecimal slab5 = income.subtract(new BigDecimal("1500000"));
            tax = tax.add(slab5.multiply(new BigDecimal("0.30")));
        }
        
        // Add 4% cess
        tax = tax.multiply(new BigDecimal("1.04"));
        
        return tax.setScale(2, RoundingMode.HALF_UP);
    }

    private String generateTaxSavingTips(TaxCalculation tax, BigDecimal totalDeductions) {
        StringBuilder tips = new StringBuilder();
        
        BigDecimal maxDeduction80c = new BigDecimal("150000");
        BigDecimal remaining80c = maxDeduction80c.subtract(tax.getDeduction80c());
        
        if (remaining80c.compareTo(BigDecimal.ZERO) > 0) {
            tips.append("💡 You can save more under 80C! Invest ₹")
                .append(remaining80c.toString())
                .append(" in PPF/ELSS/EPF to save up to ₹")
                .append(remaining80c.multiply(new BigDecimal("0.30")).setScale(0, RoundingMode.HALF_UP))
                .append(" in taxes.\n\n");
        }
        
        BigDecimal maxDeduction80d = new BigDecimal("25000");
        BigDecimal remaining80d = maxDeduction80d.subtract(tax.getDeduction80d());
        
        if (remaining80d.compareTo(BigDecimal.ZERO) > 0) {
            tips.append("🏥 Health insurance can save you tax! Pay ₹")
                .append(remaining80d.toString())
                .append(" as premium to save up to ₹")
                .append(remaining80d.multiply(new BigDecimal("0.30")).setScale(0, RoundingMode.HALF_UP))
                .append(" under 80D.\n\n");
        }
        
        if (tax.getDeduction80ccd1b().compareTo(BigDecimal.ZERO) == 0) {
            tips.append("💰 Invest ₹50,000 in NPS to get additional deduction under 80CCD(1B) and save ₹15,600 in taxes!\n\n");
        }
        
        tips.append("📊 Always compare Old vs New regime before filing returns to minimize tax liability.");
        
        return tips.toString();
    }

    public List<TaxCalculation> getUserTaxCalculations(Long userId) {
        return taxRepository.findByUserId(userId);
    }

    public TaxCalculation getLatestTaxCalculation(Long userId, String year) {
        return taxRepository.findByUserIdAndYear(userId, year).orElse(null);
    }
}