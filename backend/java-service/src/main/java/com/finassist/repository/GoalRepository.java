package com.finassist.repository;

import com.finassist.model.FinancialGoal;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class GoalRepository {

    private final JdbcTemplate jdbcTemplate;

    public GoalRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<FinancialGoal> goalRowMapper = (rs, rowNum) -> {
        FinancialGoal goal = new FinancialGoal();
        goal.setId(rs.getLong("id"));
        goal.setUserId(rs.getLong("user_id"));
        goal.setGoalName(rs.getString("goal_name"));
        goal.setGoalType(rs.getString("goal_type"));
        goal.setTargetAmount(rs.getBigDecimal("target_amount"));
        goal.setCurrentAmount(rs.getBigDecimal("current_amount"));
        goal.setTargetDate(rs.getDate("target_date").toLocalDate());
        goal.setStartDate(rs.getDate("start_date").toLocalDate());
        goal.setMonthsRemaining(rs.getInt("months_remaining"));
        goal.setMonthlySavingRequired(rs.getBigDecimal("monthly_saving_required"));
        goal.setInvestmentStrategy(rs.getString("investment_strategy"));
        goal.setRiskProfile(rs.getString("risk_profile"));
        goal.setProgressPercentage(rs.getBigDecimal("progress_percentage"));
        goal.setStatus(rs.getString("status"));
        goal.setIcon(rs.getString("icon"));
        goal.setColor(rs.getString("color"));
        goal.setPriority(rs.getInt("priority"));
        goal.setNotes(rs.getString("notes"));
        goal.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        goal.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return goal;
    };

    public FinancialGoal save(FinancialGoal goal) {
        String sql = "INSERT INTO financial_goals (user_id, goal_name, goal_type, target_amount, current_amount, " +
                     "target_date, start_date, months_remaining, monthly_saving_required, investment_strategy, " +
                     "risk_profile, status, icon, color, priority, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, goal.getUserId());
            ps.setString(2, goal.getGoalName());
            ps.setString(3, goal.getGoalType());
            ps.setBigDecimal(4, goal.getTargetAmount());
            ps.setBigDecimal(5, goal.getCurrentAmount());
            ps.setDate(6, Date.valueOf(goal.getTargetDate()));
            ps.setDate(7, Date.valueOf(goal.getStartDate()));
            ps.setInt(8, goal.getMonthsRemaining());
            ps.setBigDecimal(9, goal.getMonthlySavingRequired());
            ps.setString(10, goal.getInvestmentStrategy());
            ps.setString(11, goal.getRiskProfile());
            ps.setString(12, goal.getStatus());
            ps.setString(13, goal.getIcon());
            ps.setString(14, goal.getColor());
            ps.setInt(15, goal.getPriority());
            ps.setString(16, goal.getNotes());
            return ps;
        }, keyHolder);
        
        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null && keys.containsKey("id")) {
            goal.setId(((Number) keys.get("id")).longValue());
        }
        return goal;
    }

    public List<FinancialGoal> findByUserId(Long userId) {
        String sql = "SELECT * FROM financial_goals WHERE user_id = ? ORDER BY priority DESC, target_date ASC";
        return jdbcTemplate.query(sql, goalRowMapper, userId);
    }

    public List<FinancialGoal> findActiveGoalsByUserId(Long userId) {
        String sql = "SELECT * FROM financial_goals WHERE user_id = ? AND status = 'active' ORDER BY priority DESC";
        return jdbcTemplate.query(sql, goalRowMapper, userId);
    }

    public Optional<FinancialGoal> findById(Long id) {
        String sql = "SELECT * FROM financial_goals WHERE id = ?";
        try {
            FinancialGoal goal = jdbcTemplate.queryForObject(sql, goalRowMapper, id);
            return Optional.ofNullable(goal);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public void updateProgress(Long goalId, BigDecimal currentAmount) {
        String sql = "UPDATE financial_goals SET current_amount = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        jdbcTemplate.update(sql, currentAmount, goalId);
    }

    public void updateStatus(Long goalId, String status) {
        String sql = "UPDATE financial_goals SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        jdbcTemplate.update(sql, status, goalId);
    }

    public void delete(Long goalId) {
        String sql = "DELETE FROM financial_goals WHERE id = ?";
        jdbcTemplate.update(sql, goalId);
    }
}