-- ================================================
-- FINANCIAL ASSISTANT - COMPLETE DATABASE SCHEMA
-- ================================================

-- Drop existing tables if recreating
DROP TABLE IF EXISTS financial_goals CASCADE;
DROP TABLE IF EXISTS portfolio_transactions CASCADE;
DROP TABLE IF EXISTS portfolio_holdings CASCADE;
DROP TABLE IF EXISTS tax_calculations CASCADE;
DROP TABLE IF EXISTS documents CASCADE;
DROP TABLE IF EXISTS budget_entries CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ================================================
-- 1. USERS TABLE
-- ================================================
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    phone VARCHAR(15),
    date_of_birth DATE,
    pan_number VARCHAR(10),
    annual_income DECIMAL(12, 2),
    theme_preference VARCHAR(20) DEFAULT 'dark',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ================================================
-- 2. BUDGET ENTRIES TABLE (Enhanced)
-- ================================================
CREATE TABLE budget_entries (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    category VARCHAR(50) NOT NULL,
    subcategory VARCHAR(50),
    amount DECIMAL(10, 2) NOT NULL,
    type VARCHAR(10) NOT NULL, -- 'income' or 'expense'
    payment_method VARCHAR(50), -- 'cash', 'upi', 'credit_card', 'debit_card', 'net_banking'
    description TEXT,
    tags VARCHAR(255), -- Comma-separated tags
    entry_date DATE NOT NULL,
    is_recurring BOOLEAN DEFAULT FALSE,
    recurring_frequency VARCHAR(20), -- 'daily', 'weekly', 'monthly', 'yearly'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for faster queries
CREATE INDEX idx_budget_user_id ON budget_entries(user_id);
CREATE INDEX idx_budget_date ON budget_entries(entry_date);
CREATE INDEX idx_budget_type ON budget_entries(type);
CREATE INDEX idx_budget_category ON budget_entries(category);

-- ================================================
-- 3. DOCUMENTS TABLE (Enhanced)
-- ================================================
CREATE TABLE documents (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    filename VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(50), -- 'pdf', 'docx', 'txt', 'jpg', 'png'
    file_size INTEGER, -- in bytes
    category VARCHAR(50), -- 'tax', 'investment', 'insurance', 'bank_statement', 'other'
    extracted_text TEXT,
    metadata JSONB, -- Store extracted key-value pairs
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_documents_user_id ON documents(user_id);
CREATE INDEX idx_documents_category ON documents(category);

-- ================================================
-- 4. TAX CALCULATIONS TABLE
-- ================================================
CREATE TABLE tax_calculations (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    financial_year VARCHAR(10) NOT NULL, -- '2024-25'
    
    -- Income Details
    salary_income DECIMAL(12, 2) DEFAULT 0,
    house_property_income DECIMAL(12, 2) DEFAULT 0,
    business_income DECIMAL(12, 2) DEFAULT 0,
    capital_gains_short DECIMAL(12, 2) DEFAULT 0,
    capital_gains_long DECIMAL(12, 2) DEFAULT 0,
    other_income DECIMAL(12, 2) DEFAULT 0,
    total_income DECIMAL(12, 2) GENERATED ALWAYS AS (
        salary_income + house_property_income + business_income + 
        capital_gains_short + capital_gains_long + other_income
    ) STORED,
    
    -- Deductions (Section 80C, 80D, etc.)
    deduction_80c DECIMAL(10, 2) DEFAULT 0, -- Max 1.5L
    deduction_80d DECIMAL(10, 2) DEFAULT 0, -- Health insurance
    deduction_80ccd1b DECIMAL(10, 2) DEFAULT 0, -- NPS (50K)
    deduction_80e DECIMAL(10, 2) DEFAULT 0, -- Education loan
    deduction_80g DECIMAL(10, 2) DEFAULT 0, -- Donations
    other_deductions DECIMAL(10, 2) DEFAULT 0,
    total_deductions DECIMAL(12, 2) GENERATED ALWAYS AS (
        deduction_80c + deduction_80d + deduction_80ccd1b + 
        deduction_80e + deduction_80g + other_deductions
    ) STORED,
    
    -- Tax Calculation
    taxable_income DECIMAL(12, 2),
    tax_old_regime DECIMAL(10, 2),
    tax_new_regime DECIMAL(10, 2),
    recommended_regime VARCHAR(20),
    tax_saving_tips TEXT,
    
    calculation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tax_user_id ON tax_calculations(user_id);
CREATE INDEX idx_tax_year ON tax_calculations(financial_year);

-- ================================================
-- 5. PORTFOLIO HOLDINGS TABLE
-- ================================================
CREATE TABLE portfolio_holdings (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    asset_type VARCHAR(20) NOT NULL, -- 'stock', 'crypto', 'mutual_fund', 'gold', 'fd'
    
    -- Asset Details
    symbol VARCHAR(50) NOT NULL, -- 'RELIANCE', 'BTC', etc.
    name VARCHAR(200) NOT NULL,
    exchange VARCHAR(50), -- 'NSE', 'BSE', 'BINANCE', etc.
    
    -- Quantity and Cost
    quantity DECIMAL(18, 8) NOT NULL,
    average_buy_price DECIMAL(12, 2) NOT NULL,
    total_invested DECIMAL(12, 2) NOT NULL,
    
    -- Current Value (updated via API)
    current_price DECIMAL(12, 2),
    current_value DECIMAL(12, 2),
    unrealized_pnl DECIMAL(12, 2),
    unrealized_pnl_percentage DECIMAL(8, 2),
    
    -- Metadata
    broker VARCHAR(100),
    notes TEXT,
    last_price_update TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(user_id, symbol, asset_type)
);

CREATE INDEX idx_portfolio_user_id ON portfolio_holdings(user_id);
CREATE INDEX idx_portfolio_asset_type ON portfolio_holdings(asset_type);
CREATE INDEX idx_portfolio_symbol ON portfolio_holdings(symbol);

-- ================================================
-- 6. PORTFOLIO TRANSACTIONS TABLE
-- ================================================
CREATE TABLE portfolio_transactions (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    holding_id INTEGER REFERENCES portfolio_holdings(id) ON DELETE CASCADE,
    
    transaction_type VARCHAR(10) NOT NULL, -- 'buy', 'sell'
    asset_type VARCHAR(20) NOT NULL,
    symbol VARCHAR(50) NOT NULL,
    
    quantity DECIMAL(18, 8) NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    total_amount DECIMAL(12, 2) NOT NULL,
    fees DECIMAL(10, 2) DEFAULT 0,
    
    transaction_date DATE NOT NULL,
    broker VARCHAR(100),
    notes TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_transactions_user_id ON portfolio_transactions(user_id);
CREATE INDEX idx_transactions_holding ON portfolio_transactions(holding_id);
CREATE INDEX idx_transactions_date ON portfolio_transactions(transaction_date);

-- ================================================
-- 7. FINANCIAL GOALS TABLE
-- ================================================
CREATE TABLE financial_goals (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    
    -- Goal Details
    goal_name VARCHAR(200) NOT NULL,
    goal_type VARCHAR(50) NOT NULL, -- 'retirement', 'house', 'car', 'education', 'wedding', 'vacation', 'emergency_fund', 'custom'
    target_amount DECIMAL(12, 2) NOT NULL,
    current_amount DECIMAL(12, 2) DEFAULT 0,
    
    -- Timeline
    target_date DATE NOT NULL,
    start_date DATE DEFAULT CURRENT_DATE,
    months_remaining INTEGER,
    
    -- Planning
    monthly_saving_required DECIMAL(10, 2),
    investment_strategy TEXT,
    risk_profile VARCHAR(20), -- 'conservative', 'moderate', 'aggressive'
    
    -- Progress
    progress_percentage DECIMAL(5, 2) GENERATED ALWAYS AS (
        CASE 
            WHEN target_amount > 0 THEN (current_amount / target_amount * 100)
            ELSE 0 
        END
    ) STORED,
    status VARCHAR(20) DEFAULT 'active', -- 'active', 'completed', 'paused', 'cancelled'
    
    -- Metadata
    icon VARCHAR(50),
    color VARCHAR(20),
    priority INTEGER DEFAULT 0,
    notes TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_goals_user_id ON financial_goals(user_id);
CREATE INDEX idx_goals_status ON financial_goals(status);
CREATE INDEX idx_goals_target_date ON financial_goals(target_date);

-- ================================================
-- VIEWS FOR ANALYTICS
-- ================================================

-- Budget Summary View
CREATE OR REPLACE VIEW budget_summary AS
SELECT 
    user_id,
    DATE_TRUNC('month', entry_date) AS month,
    type,
    SUM(amount) AS total_amount,
    COUNT(*) AS transaction_count
FROM budget_entries
GROUP BY user_id, DATE_TRUNC('month', entry_date), type;

-- Portfolio Summary View
CREATE OR REPLACE VIEW portfolio_summary AS
SELECT 
    user_id,
    asset_type,
    COUNT(*) AS holdings_count,
    SUM(total_invested) AS total_invested,
    SUM(current_value) AS current_value,
    SUM(unrealized_pnl) AS total_pnl,
    CASE 
        WHEN SUM(total_invested) > 0 
        THEN (SUM(unrealized_pnl) / SUM(total_invested) * 100)
        ELSE 0 
    END AS pnl_percentage
FROM portfolio_holdings
GROUP BY user_id, asset_type;

-- ================================================
-- SAMPLE DATA FOR TESTING
-- ================================================

-- Insert test user
INSERT INTO users (username, email, password_hash, full_name, pan_number, annual_income) 
VALUES (
    'demo_user',
    'demo@example.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: demo123
    'Demo User',
    'ABCDE1234F',
    1200000.00
);

-- Insert sample budget entries
INSERT INTO budget_entries (user_id, category, amount, type, entry_date, description) VALUES
(1, 'Salary', 100000, 'income', CURRENT_DATE, 'Monthly salary'),
(1, 'Food', 8000, 'expense', CURRENT_DATE, 'Groceries and dining'),
(1, 'Transport', 3000, 'expense', CURRENT_DATE, 'Fuel and metro'),
(1, 'Entertainment', 2000, 'expense', CURRENT_DATE, 'Movies and outings');

-- Insert sample goal
INSERT INTO financial_goals (user_id, goal_name, goal_type, target_amount, current_amount, target_date) VALUES
(1, 'House Down Payment', 'house', 2000000, 500000, '2027-12-31');

-- ================================================
-- FUNCTIONS AND TRIGGERS
-- ================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers for updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_budget_updated_at BEFORE UPDATE ON budget_entries
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_portfolio_updated_at BEFORE UPDATE ON portfolio_holdings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_goals_updated_at BEFORE UPDATE ON financial_goals
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ================================================
-- INDEXES FOR PERFORMANCE
-- ================================================

-- Composite indexes for common queries
CREATE INDEX idx_budget_user_date ON budget_entries(user_id, entry_date DESC);
CREATE INDEX idx_portfolio_user_asset ON portfolio_holdings(user_id, asset_type);
CREATE INDEX idx_transactions_user_date ON portfolio_transactions(user_id, transaction_date DESC);

-- ================================================
-- GRANTS (Optional - for production)
-- ================================================

-- Create application user (optional)
-- CREATE USER finassist_app WITH PASSWORD 'secure_password';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO finassist_app;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO finassist_app;

-- ================================================
-- VERIFICATION QUERIES
-- ================================================

-- Check all tables
-- SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';

-- Check indexes
-- SELECT indexname, tablename FROM pg_indexes WHERE schemaname = 'public';

-- Check data
-- SELECT 'users' AS table_name, COUNT(*) AS count FROM users
-- UNION ALL
-- SELECT 'budget_entries', COUNT(*) FROM budget_entries
-- UNION ALL
-- SELECT 'portfolio_holdings', COUNT(*) FROM portfolio_holdings
-- UNION ALL
-- SELECT 'financial_goals', COUNT(*) FROM financial_goals;