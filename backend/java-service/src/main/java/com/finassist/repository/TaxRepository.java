package com.finassist.repository;

import com.finassist.model.TaxCalculation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TaxRepository {

    private final JdbcTemplate jdbcTemplate;

    public TaxRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<TaxCalculation> taxRowMapper = (rs, rowNum) -> {
        TaxCalculation tax = new TaxCalculation();
        tax.setId(rs.getLong("id"));
        tax.setUserId(rs.getLong("user_id"));
        tax.setFinancialYear(rs.getString("financial_year"));
        tax.setSalaryIncome(rs.getBigDecimal("salary_income"));
        tax.setHousePropertyIncome(rs.getBigDecimal("house_property_income"));
        tax.setBusinessIncome(rs.getBigDecimal("business_income"));
        tax.setCapitalGainsShort(rs.getBigDecimal("capital_gains_short"));
        tax.setCapitalGainsLong(rs.getBigDecimal("capital_gains_long"));
        tax.setOtherIncome(rs.getBigDecimal("other_income"));
        tax.setTotalIncome(rs.getBigDecimal("total_income"));
        tax.setDeduction80c(rs.getBigDecimal("deduction_80c"));
        tax.setDeduction80d(rs.getBigDecimal("deduction_80d"));
        tax.setDeduction80ccd1b(rs.getBigDecimal("deduction_80ccd1b"));
        tax.setDeduction80e(rs.getBigDecimal("deduction_80e"));
        tax.setDeduction80g(rs.getBigDecimal("deduction_80g"));
        tax.setOtherDeductions(rs.getBigDecimal("other_deductions"));
        tax.setTotalDeductions(rs.getBigDecimal("total_deductions"));
        tax.setTaxableIncome(rs.getBigDecimal("taxable_income"));
        tax.setTaxOldRegime(rs.getBigDecimal("tax_old_regime"));
        tax.setTaxNewRegime(rs.getBigDecimal("tax_new_regime"));
        tax.setRecommendedRegime(rs.getString("recommended_regime"));
        tax.setTaxSavingTips(rs.getString("tax_saving_tips"));
        tax.setCalculationDate(rs.getTimestamp("calculation_date").toLocalDateTime());
        tax.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return tax;
    };

    public TaxCalculation save(TaxCalculation tax) {
        String sql = "INSERT INTO tax_calculations (user_id, financial_year, salary_income, " +
                     "house_property_income, business_income, capital_gains_short, capital_gains_long, " +
                     "other_income, deduction_80c, deduction_80d, deduction_80ccd1b, deduction_80e, " +
                     "deduction_80g, other_deductions, taxable_income, tax_old_regime, tax_new_regime, " +
                     "recommended_regime, tax_saving_tips) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, tax.getUserId());
            ps.setString(2, tax.getFinancialYear());
            ps.setBigDecimal(3, tax.getSalaryIncome());
            ps.setBigDecimal(4, tax.getHousePropertyIncome());
            ps.setBigDecimal(5, tax.getBusinessIncome());
            ps.setBigDecimal(6, tax.getCapitalGainsShort());
            ps.setBigDecimal(7, tax.getCapitalGainsLong());
            ps.setBigDecimal(8, tax.getOtherIncome());
            ps.setBigDecimal(9, tax.getDeduction80c());
            ps.setBigDecimal(10, tax.getDeduction80d());
            ps.setBigDecimal(11, tax.getDeduction80ccd1b());
            ps.setBigDecimal(12, tax.getDeduction80e());
            ps.setBigDecimal(13, tax.getDeduction80g());
            ps.setBigDecimal(14, tax.getOtherDeductions());
            ps.setBigDecimal(15, tax.getTaxableIncome());
            ps.setBigDecimal(16, tax.getTaxOldRegime());
            ps.setBigDecimal(17, tax.getTaxNewRegime());
            ps.setString(18, tax.getRecommendedRegime());
            ps.setString(19, tax.getTaxSavingTips());
            return ps;
        }, keyHolder);
        
        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null && keys.containsKey("id")) {
            tax.setId(((Number) keys.get("id")).longValue());
        }
        return tax;
    }

    public List<TaxCalculation> findByUserId(Long userId) {
        String sql = "SELECT * FROM tax_calculations WHERE user_id = ? ORDER BY financial_year DESC";
        return jdbcTemplate.query(sql, taxRowMapper, userId);
    }

    public Optional<TaxCalculation> findByUserIdAndYear(Long userId, String year) {
        String sql = "SELECT * FROM tax_calculations WHERE user_id = ? AND financial_year = ? ORDER BY created_at DESC LIMIT 1";
        try {
            TaxCalculation tax = jdbcTemplate.queryForObject(sql, taxRowMapper, userId, year);
            return Optional.ofNullable(tax);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}