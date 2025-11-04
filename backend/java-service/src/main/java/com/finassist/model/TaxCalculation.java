package com.finassist.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TaxCalculation {
    private Long id;
    private Long userId;
    private String financialYear;
    private BigDecimal salaryIncome;
    private BigDecimal housePropertyIncome;
    private BigDecimal businessIncome;
    private BigDecimal capitalGainsShort;
    private BigDecimal capitalGainsLong;
    private BigDecimal otherIncome;
    private BigDecimal totalIncome;
    private BigDecimal deduction80c;
    private BigDecimal deduction80d;
    private BigDecimal deduction80ccd1b;
    private BigDecimal deduction80e;
    private BigDecimal deduction80g;
    private BigDecimal otherDeductions;
    private BigDecimal totalDeductions;
    private BigDecimal taxableIncome;
    private BigDecimal taxOldRegime;
    private BigDecimal taxNewRegime;
    private String recommendedRegime;
    private String taxSavingTips;
    private LocalDateTime calculationDate;
    private LocalDateTime createdAt;

    public TaxCalculation() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getFinancialYear() { return financialYear; }
    public void setFinancialYear(String financialYear) { this.financialYear = financialYear; }
    
    public BigDecimal getSalaryIncome() { return salaryIncome; }
    public void setSalaryIncome(BigDecimal salaryIncome) { this.salaryIncome = salaryIncome; }
    
    public BigDecimal getHousePropertyIncome() { return housePropertyIncome; }
    public void setHousePropertyIncome(BigDecimal housePropertyIncome) { 
        this.housePropertyIncome = housePropertyIncome; 
    }
    
    public BigDecimal getBusinessIncome() { return businessIncome; }
    public void setBusinessIncome(BigDecimal businessIncome) { this.businessIncome = businessIncome; }
    
    public BigDecimal getCapitalGainsShort() { return capitalGainsShort; }
    public void setCapitalGainsShort(BigDecimal capitalGainsShort) { 
        this.capitalGainsShort = capitalGainsShort; 
    }
    
    public BigDecimal getCapitalGainsLong() { return capitalGainsLong; }
    public void setCapitalGainsLong(BigDecimal capitalGainsLong) { 
        this.capitalGainsLong = capitalGainsLong; 
    }
    
    public BigDecimal getOtherIncome() { return otherIncome; }
    public void setOtherIncome(BigDecimal otherIncome) { this.otherIncome = otherIncome; }
    
    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }
    
    public BigDecimal getDeduction80c() { return deduction80c; }
    public void setDeduction80c(BigDecimal deduction80c) { this.deduction80c = deduction80c; }
    
    public BigDecimal getDeduction80d() { return deduction80d; }
    public void setDeduction80d(BigDecimal deduction80d) { this.deduction80d = deduction80d; }
    
    public BigDecimal getDeduction80ccd1b() { return deduction80ccd1b; }
    public void setDeduction80ccd1b(BigDecimal deduction80ccd1b) { 
        this.deduction80ccd1b = deduction80ccd1b; 
    }
    
    public BigDecimal getDeduction80e() { return deduction80e; }
    public void setDeduction80e(BigDecimal deduction80e) { this.deduction80e = deduction80e; }
    
    public BigDecimal getDeduction80g() { return deduction80g; }
    public void setDeduction80g(BigDecimal deduction80g) { this.deduction80g = deduction80g; }
    
    public BigDecimal getOtherDeductions() { return otherDeductions; }
    public void setOtherDeductions(BigDecimal otherDeductions) { this.otherDeductions = otherDeductions; }
    
    public BigDecimal getTotalDeductions() { return totalDeductions; }
    public void setTotalDeductions(BigDecimal totalDeductions) { this.totalDeductions = totalDeductions; }
    
    public BigDecimal getTaxableIncome() { return taxableIncome; }
    public void setTaxableIncome(BigDecimal taxableIncome) { this.taxableIncome = taxableIncome; }
    
    public BigDecimal getTaxOldRegime() { return taxOldRegime; }
    public void setTaxOldRegime(BigDecimal taxOldRegime) { this.taxOldRegime = taxOldRegime; }
    
    public BigDecimal getTaxNewRegime() { return taxNewRegime; }
    public void setTaxNewRegime(BigDecimal taxNewRegime) { this.taxNewRegime = taxNewRegime; }
    
    public String getRecommendedRegime() { return recommendedRegime; }
    public void setRecommendedRegime(String recommendedRegime) { this.recommendedRegime = recommendedRegime; }
    
    public String getTaxSavingTips() { return taxSavingTips; }
    public void setTaxSavingTips(String taxSavingTips) { this.taxSavingTips = taxSavingTips; }
    
    public LocalDateTime getCalculationDate() { return calculationDate; }
    public void setCalculationDate(LocalDateTime calculationDate) { this.calculationDate = calculationDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}