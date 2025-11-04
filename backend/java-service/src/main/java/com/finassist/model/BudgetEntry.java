package com.finassist.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class BudgetEntry {
    private Long id;
    private Long userId;
    private String category;
    private BigDecimal amount;
    private String type; // 'income' or 'expense'
    private String description;
    private LocalDate entryDate;
    private LocalDateTime createdAt;

    // Constructors
    public BudgetEntry() {}

    public BudgetEntry(Long userId, String category, BigDecimal amount, String type, 
                      String description, LocalDate entryDate) {
        this.userId = userId;
        this.category = category;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.entryDate = entryDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}