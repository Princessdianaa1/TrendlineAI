package com.finassist.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class FinancialGoal {
    private Long id;
    private Long userId;
    private String goalName;
    private String goalType;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate targetDate;
    private LocalDate startDate;
    private Integer monthsRemaining;
    private BigDecimal monthlySavingRequired;
    private String investmentStrategy;
    private String riskProfile;
    private BigDecimal progressPercentage;
    private String status;
    private String icon;
    private String color;
    private Integer priority;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FinancialGoal() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getGoalName() { return goalName; }
    public void setGoalName(String goalName) { this.goalName = goalName; }

    public String getGoalType() { return goalType; }
    public void setGoalType(String goalType) { this.goalType = goalType; }

    public BigDecimal getTargetAmount() { return targetAmount; }
    public void setTargetAmount(BigDecimal targetAmount) { this.targetAmount = targetAmount; }

    public BigDecimal getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(BigDecimal currentAmount) { this.currentAmount = currentAmount; }

    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public Integer getMonthsRemaining() { return monthsRemaining; }
    public void setMonthsRemaining(Integer monthsRemaining) { this.monthsRemaining = monthsRemaining; }

    public BigDecimal getMonthlySavingRequired() { return monthlySavingRequired; }
    public void setMonthlySavingRequired(BigDecimal monthlySavingRequired) { 
        this.monthlySavingRequired = monthlySavingRequired; 
    }

    public String getInvestmentStrategy() { return investmentStrategy; }
    public void setInvestmentStrategy(String investmentStrategy) { 
        this.investmentStrategy = investmentStrategy; 
    }

    public String getRiskProfile() { return riskProfile; }
    public void setRiskProfile(String riskProfile) { this.riskProfile = riskProfile; }

    public BigDecimal getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(BigDecimal progressPercentage) { 
        this.progressPercentage = progressPercentage; 
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}