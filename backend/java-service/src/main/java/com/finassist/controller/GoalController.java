package com.finassist.controller;

import com.finassist.model.FinancialGoal;
import com.finassist.service.GoalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping("/create")
    public ResponseEntity<FinancialGoal> createGoal(@RequestBody FinancialGoal goal) {
        FinancialGoal created = goalService.createGoal(goal);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FinancialGoal>> getUserGoals(@PathVariable Long userId) {
        List<FinancialGoal> goals = goalService.getUserGoals(userId);
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<FinancialGoal>> getActiveGoals(@PathVariable Long userId) {
        List<FinancialGoal> goals = goalService.getActiveGoals(userId);
        return ResponseEntity.ok(goals);
    }

    @PutMapping("/{goalId}/progress")
    public ResponseEntity<FinancialGoal> updateProgress(
            @PathVariable Long goalId, 
            @RequestBody Map<String, BigDecimal> request) {
        BigDecimal amount = request.get("amount");
        FinancialGoal updated = goalService.updateGoalProgress(goalId, amount);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{goalId}")
    public ResponseEntity<Void> deleteGoal(@PathVariable Long goalId) {
        goalService.deleteGoal(goalId);
        return ResponseEntity.ok().build();
    }
}