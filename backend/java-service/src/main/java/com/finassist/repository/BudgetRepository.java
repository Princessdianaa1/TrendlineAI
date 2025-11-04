package com.finassist.repository;

import com.finassist.model.BudgetEntry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@Repository
public class BudgetRepository {

    private final JdbcTemplate jdbcTemplate;

    public BudgetRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<BudgetEntry> budgetEntryRowMapper = (rs, rowNum) -> {
        BudgetEntry entry = new BudgetEntry();
        entry.setId(rs.getLong("id"));
        entry.setUserId(rs.getLong("user_id"));
        entry.setCategory(rs.getString("category"));
        entry.setAmount(rs.getBigDecimal("amount"));
        entry.setType(rs.getString("type"));
        entry.setDescription(rs.getString("description"));
        entry.setEntryDate(rs.getDate("entry_date").toLocalDate());
        entry.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return entry;
    };

    public BudgetEntry save(BudgetEntry entry) {
        String sql = "INSERT INTO budget_entries (user_id, category, amount, type, description, entry_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, entry.getUserId());
            ps.setString(2, entry.getCategory());
            ps.setBigDecimal(3, entry.getAmount());
            ps.setString(4, entry.getType());
            ps.setString(5, entry.getDescription());
            ps.setDate(6, Date.valueOf(entry.getEntryDate()));
            return ps;
        }, keyHolder);
        
        // FIX: Get the ID from the key map
        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null && keys.containsKey("id")) {
            entry.setId(((Number) keys.get("id")).longValue());
        }
        
        return entry;
    }

    public List<BudgetEntry> findByUserId(Long userId) {
        String sql = "SELECT * FROM budget_entries WHERE user_id = ? ORDER BY entry_date DESC";
        return jdbcTemplate.query(sql, budgetEntryRowMapper, userId);
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM budget_entries WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public List<BudgetEntry> findByUserIdAndType(Long userId, String type) {
        String sql = "SELECT * FROM budget_entries WHERE user_id = ? AND type = ? ORDER BY entry_date DESC";
        return jdbcTemplate.query(sql, budgetEntryRowMapper, userId, type);
    }
}