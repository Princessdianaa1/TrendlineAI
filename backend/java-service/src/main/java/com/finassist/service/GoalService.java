package com.finassist.service;

import com.finassist.model.FinancialGoal;
import com.finassist.repository.GoalRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class GoalService {

    private final GoalRepository goalRepository;

    public GoalService(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }

    public FinancialGoal createGoal(FinancialGoal goal) {
        // Calculate months remaining
        long monthsRemaining = ChronoUnit.MONTHS.between(LocalDate.now(), goal.getTargetDate());
        goal.setMonthsRemaining((int) monthsRemaining);
        
        // Calculate monthly saving required
        if (monthsRemaining > 0) {
            BigDecimal remaining = goal.getTargetAmount().subtract(goal.getCurrentAmount());
            BigDecimal monthlySaving = remaining.divide(
                new BigDecimal(monthsRemaining), 
                2, 
                RoundingMode.HALF_UP
            );
            goal.setMonthlySavingRequired(monthlySaving);
        }
        
        // Generate investment strategy based on timeline and risk profile
        goal.setInvestmentStrategy(generateInvestmentStrategy(goal));
        
        return goalRepository.save(goal);
    }

    public List<FinancialGoal> getUserGoals(Long userId) {
        return goalRepository.findByUserId(userId);
    }

    public List<FinancialGoal> getActiveGoals(Long userId) {
        return goalRepository.findActiveGoalsByUserId(userId);
    }

    public FinancialGoal updateGoalProgress(Long goalId, BigDecimal amount) {
        var goalOpt = goalRepository.findById(goalId);
        if (goalOpt.isPresent()) {
            FinancialGoal goal = goalOpt.get();
            BigDecimal newAmount = goal.getCurrentAmount().add(amount);
            
            // Check if goal is completed
            if (newAmount.compareTo(goal.getTargetAmount()) >= 0) {
                goalRepository.updateStatus(goalId, "completed");
            }
            
            goalRepository.updateProgress(goalId, newAmount);
            return goalRepository.findById(goalId).orElse(null);
        }
        return null;
    }

    public void deleteGoal(Long goalId) {
        goalRepository.delete(goalId);
    }

    private String generateInvestmentStrategy(FinancialGoal goal) {
        int months = goal.getMonthsRemaining();
        String riskProfile = goal.getRiskProfile();
        
        StringBuilder strategy = new StringBuilder();
        
        if (months <= 12) {
            strategy.append("🔒 Short-term (< 1 year): Use safe instruments\n");
            strategy.append("• Liquid Funds (7-7.5% returns)\n");
            strategy.append("• Short-term FDs (6.5-7% returns)\n");
            strategy.append("• Savings account for 3 months expenses\n");
        } else if (months <= 36) {
            strategy.append("📊 Medium-term (1-3 years): Balanced approach\n");
            strategy.append("• Debt Funds (8-9% returns): 60%\n");
            strategy.append("• Conservative Hybrid Funds: 30%\n");
            strategy.append("• Liquid Funds: 10%\n");
        } else {
            if ("aggressive".equals(riskProfile)) {
                strategy.append("🚀 Long-term Aggressive:\n");
                strategy.append("• Equity Mutual Funds (12-15% returns): 70%\n");
                strategy.append("• Mid/Small Cap Funds: 20%\n");
                strategy.append("• Debt Funds: 10%\n");
            } else if ("moderate".equals(riskProfile)) {
                strategy.append("⚖️ Long-term Moderate:\n");
                strategy.append("• Equity Mutual Funds: 50%\n");
                strategy.append("• Balanced Hybrid Funds: 30%\n");
                strategy.append("• Debt Funds: 20%\n");
            } else {
                strategy.append("🛡️ Long-term Conservative:\n");
                strategy.append("• PPF (7.1% tax-free): 40%\n");
                strategy.append("• Debt Funds: 30%\n");
                strategy.append("• Equity Funds: 30%\n");
            }
        }
        
        return strategy.toString();
    }
}