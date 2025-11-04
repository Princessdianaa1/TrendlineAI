package com.finassist.service;

import com.finassist.model.PortfolioHolding;
import com.finassist.model.PortfolioTransaction;
import com.finassist.repository.PortfolioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    public PortfolioService(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    /**
     * Add or update a portfolio holding
     * Automatically calculates current value and P&L if current price is provided
     */
    public PortfolioHolding addOrUpdateHolding(PortfolioHolding holding) {
        // Calculate current values if not provided
        if (holding.getCurrentPrice() != null && holding.getQuantity() != null) {
            BigDecimal currentValue = holding.getQuantity().multiply(holding.getCurrentPrice());
            BigDecimal unrealizedPnl = currentValue.subtract(holding.getTotalInvested());
            BigDecimal unrealizedPnlPercentage = holding.getTotalInvested().compareTo(BigDecimal.ZERO) > 0
                    ? unrealizedPnl.divide(holding.getTotalInvested(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                    : BigDecimal.ZERO;
            
            holding.setCurrentValue(currentValue);
            holding.setUnrealizedPnl(unrealizedPnl);
            holding.setUnrealizedPnlPercentage(unrealizedPnlPercentage);
        }
        
        return portfolioRepository.saveHolding(holding);
    }

    /**
     * Add a transaction and update the corresponding holding
     */
    public PortfolioTransaction addTransaction(PortfolioTransaction transaction) {
        // Save transaction
        transaction = portfolioRepository.saveTransaction(transaction);
        
        // Update holding
        Optional<PortfolioHolding> holdingOpt = portfolioRepository.findHoldingBySymbol(
            transaction.getUserId(), 
            transaction.getSymbol(), 
            transaction.getAssetType()
        );
        
        if (holdingOpt.isPresent()) {
            PortfolioHolding holding = holdingOpt.get();
            
            if ("buy".equals(transaction.getTransactionType())) {
                // Update for buy transaction
                BigDecimal newQuantity = holding.getQuantity().add(transaction.getQuantity());
                BigDecimal newTotalInvested = holding.getTotalInvested().add(transaction.getTotalAmount());
                BigDecimal newAvgPrice = newTotalInvested.divide(newQuantity, 2, RoundingMode.HALF_UP);
                
                holding.setQuantity(newQuantity);
                holding.setAverageBuyPrice(newAvgPrice);
                holding.setTotalInvested(newTotalInvested);
                
            } else if ("sell".equals(transaction.getTransactionType())) {
                // Update for sell transaction
                BigDecimal newQuantity = holding.getQuantity().subtract(transaction.getQuantity());
                BigDecimal soldInvestment = holding.getAverageBuyPrice().multiply(transaction.getQuantity());
                BigDecimal newTotalInvested = holding.getTotalInvested().subtract(soldInvestment);
                
                holding.setQuantity(newQuantity);
                holding.setTotalInvested(newTotalInvested);
                
                // Delete holding if quantity is zero or negative
                if (newQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                    portfolioRepository.deleteHolding(holding.getId());
                    return transaction;
                }
            }
            
            // Recalculate P&L if current price is available
            if (holding.getCurrentPrice() != null) {
                BigDecimal currentValue = holding.getQuantity().multiply(holding.getCurrentPrice());
                BigDecimal unrealizedPnl = currentValue.subtract(holding.getTotalInvested());
                BigDecimal unrealizedPnlPercentage = holding.getTotalInvested().compareTo(BigDecimal.ZERO) > 0
                        ? unrealizedPnl.divide(holding.getTotalInvested(), 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"))
                        : BigDecimal.ZERO;
                
                holding.setCurrentValue(currentValue);
                holding.setUnrealizedPnl(unrealizedPnl);
                holding.setUnrealizedPnlPercentage(unrealizedPnlPercentage);
            }
            
            portfolioRepository.saveHolding(holding);
        }
        
        return transaction;
    }

    /**
     * Get all holdings for a user
     */
    public List<PortfolioHolding> getUserHoldings(Long userId) {
        return portfolioRepository.findHoldingsByUserId(userId);
    }

    /**
     * Get holdings by asset type (e.g., stocks, mutual_funds, etc.)
     */
    public List<PortfolioHolding> getUserHoldingsByType(Long userId, String assetType) {
        return portfolioRepository.findHoldingsByUserIdAndAssetType(userId, assetType);
    }

    /**
     * Get all transactions for a user
     */
    public List<PortfolioTransaction> getUserTransactions(Long userId) {
        return portfolioRepository.findTransactionsByUserId(userId);
    }

    /**
     * Update the current price of a holding and recalculate P&L
     * This can be used for periodic price updates from external APIs
     */
    public void updateHoldingPrice(Long holdingId, BigDecimal currentPrice) {
        Optional<PortfolioHolding> holdingOpt = portfolioRepository.findById(holdingId);
        
        if (holdingOpt.isPresent()) {
            PortfolioHolding holding = holdingOpt.get();
            
            // Calculate new values
            BigDecimal currentValue = holding.getQuantity().multiply(currentPrice);
            BigDecimal unrealizedPnl = currentValue.subtract(holding.getTotalInvested());
            BigDecimal unrealizedPnlPercentage = holding.getTotalInvested().compareTo(BigDecimal.ZERO) > 0
                    ? unrealizedPnl.divide(holding.getTotalInvested(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                    : BigDecimal.ZERO;
            
            // Update prices in database
            portfolioRepository.updateHoldingPrices(
                holdingId, 
                currentPrice, 
                currentValue, 
                unrealizedPnl, 
                unrealizedPnlPercentage
            );
        }
    }

    /**
     * Update prices for all holdings of a user
     * Useful for bulk price updates
     */
    public void updateAllHoldingPrices(Long userId, java.util.Map<String, BigDecimal> symbolPriceMap) {
        List<PortfolioHolding> holdings = getUserHoldings(userId);
        
        for (PortfolioHolding holding : holdings) {
            String symbol = holding.getSymbol();
            if (symbolPriceMap.containsKey(symbol)) {
                BigDecimal newPrice = symbolPriceMap.get(symbol);
                updateHoldingPrice(holding.getId(), newPrice);
            }
        }
    }

    /**
     * Delete a holding by ID
     */
    public void deleteHolding(Long holdingId) {
        portfolioRepository.deleteHolding(holdingId);
    }

    /**
     * Get total portfolio value for a user
     */
    public BigDecimal getTotalPortfolioValue(Long userId) {
        List<PortfolioHolding> holdings = getUserHoldings(userId);
        
        return holdings.stream()
            .map(holding -> holding.getCurrentValue() != null 
                ? holding.getCurrentValue() 
                : holding.getTotalInvested())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get total invested amount for a user
     */
    public BigDecimal getTotalInvested(Long userId) {
        List<PortfolioHolding> holdings = getUserHoldings(userId);
        
        return holdings.stream()
            .map(PortfolioHolding::getTotalInvested)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get total unrealized P&L for a user
     */
    public BigDecimal getTotalUnrealizedPnL(Long userId) {
        List<PortfolioHolding> holdings = getUserHoldings(userId);
        
        return holdings.stream()
            .map(holding -> holding.getUnrealizedPnl() != null 
                ? holding.getUnrealizedPnl() 
                : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}