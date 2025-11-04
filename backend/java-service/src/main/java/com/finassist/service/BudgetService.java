package com.finassist.service;

import com.finassist.model.BudgetEntry;
import com.finassist.repository.BudgetRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;

    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    public BudgetEntry addEntry(BudgetEntry entry) {
        return budgetRepository.save(entry);
    }

    public List<BudgetEntry> getUserEntries(Long userId) {
        return budgetRepository.findByUserId(userId);
    }

    public void deleteEntry(Long id) {
        budgetRepository.deleteById(id);
    }

    public List<BudgetEntry> getUserEntriesByType(Long userId, String type) {
        return budgetRepository.findByUserIdAndType(userId, type);
    }
}