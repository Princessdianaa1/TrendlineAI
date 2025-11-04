package com.finassist.repository;

import com.finassist.model.PortfolioHolding;
import com.finassist.model.PortfolioTransaction;
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
public class PortfolioRepository {

    private final JdbcTemplate jdbcTemplate;

    public PortfolioRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<PortfolioHolding> holdingRowMapper = (rs, rowNum) -> {
        PortfolioHolding holding = new PortfolioHolding();
        holding.setId(rs.getLong("id"));
        holding.setUserId(rs.getLong("user_id"));
        holding.setAssetType(rs.getString("asset_type"));
        holding.setSymbol(rs.getString("symbol"));
        holding.setName(rs.getString("name"));
        holding.setExchange(rs.getString("exchange"));
        holding.setQuantity(rs.getBigDecimal("quantity"));
        holding.setAverageBuyPrice(rs.getBigDecimal("average_buy_price"));
        holding.setTotalInvested(rs.getBigDecimal("total_invested"));
        holding.setCurrentPrice(rs.getBigDecimal("current_price"));
        holding.setCurrentValue(rs.getBigDecimal("current_value"));
        holding.setUnrealizedPnl(rs.getBigDecimal("unrealized_pnl"));
        holding.setUnrealizedPnlPercentage(rs.getBigDecimal("unrealized_pnl_percentage"));
        holding.setBroker(rs.getString("broker"));
        holding.setNotes(rs.getString("notes"));
        if (rs.getTimestamp("last_price_update") != null) {
            holding.setLastPriceUpdate(rs.getTimestamp("last_price_update").toLocalDateTime());
        }
        holding.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        holding.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return holding;
    };

    private final RowMapper<PortfolioTransaction> transactionRowMapper = (rs, rowNum) -> {
        PortfolioTransaction transaction = new PortfolioTransaction();
        transaction.setId(rs.getLong("id"));
        transaction.setUserId(rs.getLong("user_id"));
        transaction.setHoldingId(rs.getLong("holding_id"));
        transaction.setTransactionType(rs.getString("transaction_type"));
        transaction.setAssetType(rs.getString("asset_type"));
        transaction.setSymbol(rs.getString("symbol"));
        transaction.setQuantity(rs.getBigDecimal("quantity"));
        transaction.setPrice(rs.getBigDecimal("price"));
        transaction.setTotalAmount(rs.getBigDecimal("total_amount"));
        transaction.setFees(rs.getBigDecimal("fees"));
        transaction.setTransactionDate(rs.getDate("transaction_date").toLocalDate());
        transaction.setBroker(rs.getString("broker"));
        transaction.setNotes(rs.getString("notes"));
        transaction.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return transaction;
    };

    public PortfolioHolding saveHolding(PortfolioHolding holding) {
        String sql = "INSERT INTO portfolio_holdings (user_id, asset_type, symbol, name, exchange, quantity, " +
                     "average_buy_price, total_invested, current_price, current_value, unrealized_pnl, " +
                     "unrealized_pnl_percentage, broker, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (user_id, symbol, asset_type) DO UPDATE SET " +
                     "quantity = EXCLUDED.quantity, average_buy_price = EXCLUDED.average_buy_price, " +
                     "total_invested = EXCLUDED.total_invested, updated_at = CURRENT_TIMESTAMP";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, holding.getUserId());
            ps.setString(2, holding.getAssetType());
            ps.setString(3, holding.getSymbol());
            ps.setString(4, holding.getName());
            ps.setString(5, holding.getExchange());
            ps.setBigDecimal(6, holding.getQuantity());
            ps.setBigDecimal(7, holding.getAverageBuyPrice());
            ps.setBigDecimal(8, holding.getTotalInvested());
            ps.setBigDecimal(9, holding.getCurrentPrice());
            ps.setBigDecimal(10, holding.getCurrentValue());
            ps.setBigDecimal(11, holding.getUnrealizedPnl());
            ps.setBigDecimal(12, holding.getUnrealizedPnlPercentage());
            ps.setString(13, holding.getBroker());
            ps.setString(14, holding.getNotes());
            return ps;
        }, keyHolder);
        
        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null && keys.containsKey("id")) {
            holding.setId(((Number) keys.get("id")).longValue());
        }
        return holding;
    }

    // NEW METHOD: Find holding by ID
    public Optional<PortfolioHolding> findById(Long holdingId) {
        String sql = "SELECT * FROM portfolio_holdings WHERE id = ?";
        try {
            PortfolioHolding holding = jdbcTemplate.queryForObject(sql, holdingRowMapper, holdingId);
            return Optional.ofNullable(holding);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<PortfolioHolding> findHoldingsByUserId(Long userId) {
        String sql = "SELECT * FROM portfolio_holdings WHERE user_id = ? ORDER BY total_invested DESC";
        return jdbcTemplate.query(sql, holdingRowMapper, userId);
    }

    public List<PortfolioHolding> findHoldingsByUserIdAndAssetType(Long userId, String assetType) {
        String sql = "SELECT * FROM portfolio_holdings WHERE user_id = ? AND asset_type = ?";
        return jdbcTemplate.query(sql, holdingRowMapper, userId, assetType);
    }

    public Optional<PortfolioHolding> findHoldingBySymbol(Long userId, String symbol, String assetType) {
        String sql = "SELECT * FROM portfolio_holdings WHERE user_id = ? AND symbol = ? AND asset_type = ?";
        try {
            PortfolioHolding holding = jdbcTemplate.queryForObject(sql, holdingRowMapper, userId, symbol, assetType);
            return Optional.ofNullable(holding);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public void updateHoldingPrices(Long holdingId, BigDecimal currentPrice, BigDecimal currentValue, 
                                    BigDecimal unrealizedPnl, BigDecimal unrealizedPnlPercentage) {
        String sql = "UPDATE portfolio_holdings SET current_price = ?, current_value = ?, " +
                     "unrealized_pnl = ?, unrealized_pnl_percentage = ?, last_price_update = CURRENT_TIMESTAMP " +
                     "WHERE id = ?";
        jdbcTemplate.update(sql, currentPrice, currentValue, unrealizedPnl, unrealizedPnlPercentage, holdingId);
    }

    public PortfolioTransaction saveTransaction(PortfolioTransaction transaction) {
        String sql = "INSERT INTO portfolio_transactions (user_id, holding_id, transaction_type, asset_type, " +
                     "symbol, quantity, price, total_amount, fees, transaction_date, broker, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, transaction.getUserId());
            ps.setLong(2, transaction.getHoldingId());
            ps.setString(3, transaction.getTransactionType());
            ps.setString(4, transaction.getAssetType());
            ps.setString(5, transaction.getSymbol());
            ps.setBigDecimal(6, transaction.getQuantity());
            ps.setBigDecimal(7, transaction.getPrice());
            ps.setBigDecimal(8, transaction.getTotalAmount());
            ps.setBigDecimal(9, transaction.getFees());
            ps.setDate(10, Date.valueOf(transaction.getTransactionDate()));
            ps.setString(11, transaction.getBroker());
            ps.setString(12, transaction.getNotes());
            return ps;
        }, keyHolder);
        
        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null && keys.containsKey("id")) {
            transaction.setId(((Number) keys.get("id")).longValue());
        }
        return transaction;
    }

    public List<PortfolioTransaction> findTransactionsByUserId(Long userId) {
        String sql = "SELECT * FROM portfolio_transactions WHERE user_id = ? ORDER BY transaction_date DESC LIMIT 100";
        return jdbcTemplate.query(sql, transactionRowMapper, userId);
    }

    public void deleteHolding(Long holdingId) {
        String sql = "DELETE FROM portfolio_holdings WHERE id = ?";
        jdbcTemplate.update(sql, holdingId);
    }
}