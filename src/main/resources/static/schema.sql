-- =========================================================
-- AGRITRADE DATABASE SCHEMA - PRODUCTION READY
-- =========================================================

-- First, connect to postgres (system database)
-- Run this part separately or ensure you're connected to postgres, not agritrade

-- Drop existing database if it exists (run while connected to postgres)
-- DROP DATABASE IF EXISTS agritrade;

-- Create the database (run while connected to postgres)
-- CREATE DATABASE agritrade;

-- Now connect to agritrade database and run the rest:
-- \c agritrade;

-- =========================================================
-- ENUM TYPES
-- =========================================================

CREATE TYPE user_role AS ENUM ('FARMER', 'RETAILER', 'ADMIN');
CREATE TYPE crop_status AS ENUM ('AVAILABLE', 'BIDDING_CLOSED', 'SOLD');
CREATE TYPE bid_status AS ENUM ('PENDING', 'ACCEPTED', 'REJECTED');
CREATE TYPE order_status AS ENUM ('PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED');
CREATE TYPE payment_status AS ENUM ('PENDING', 'COMPLETED', 'FAILED');

-- =========================================================
-- DROP EXISTING TABLES (if they exist)
-- =========================================================

DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS payments CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS bids CASCADE;
DROP TABLE IF EXISTS crop_batches CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Drop existing types if they exist
DROP TYPE IF EXISTS user_role CASCADE;
DROP TYPE IF EXISTS crop_status CASCADE;
DROP TYPE IF EXISTS bid_status CASCADE;
DROP TYPE IF EXISTS order_status CASCADE;
DROP TYPE IF EXISTS payment_status CASCADE;

-- Recreate types
CREATE TYPE user_role AS ENUM ('FARMER', 'RETAILER', 'ADMIN');
CREATE TYPE crop_status AS ENUM ('AVAILABLE', 'BIDDING_CLOSED', 'SOLD');
CREATE TYPE bid_status AS ENUM ('PENDING', 'ACCEPTED', 'REJECTED');
CREATE TYPE order_status AS ENUM ('PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED');
CREATE TYPE payment_status AS ENUM ('PENDING', 'COMPLETED', 'FAILED');

-- =========================================================
-- USERS TABLE
-- =========================================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100),
    phone_number VARCHAR(15),
    role user_role NOT NULL,
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================================================
-- CROP BATCHES TABLE
-- =========================================================
CREATE TABLE crop_batches (
    id BIGSERIAL PRIMARY KEY,
    farmer_id BIGINT NOT NULL REFERENCES users(id),
    crop_name VARCHAR(100) NOT NULL,
    crop_type VARCHAR(50),
    quantity DECIMAL(10,2) NOT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    harvest_date DATE,
    expiry_date DATE,
    description TEXT,
    status crop_status DEFAULT 'AVAILABLE',
    image_url VARCHAR(500),
    location VARCHAR(200),
    is_organic BOOLEAN DEFAULT FALSE,
    unit VARCHAR(20) DEFAULT 'kg',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================================================
-- BIDS TABLE
-- =========================================================
CREATE TABLE bids (
    id BIGSERIAL PRIMARY KEY,
    crop_batch_id BIGINT NOT NULL REFERENCES crop_batches(id),
    retailer_id BIGINT NOT NULL REFERENCES users(id),
    bid_amount DECIMAL(10,2) NOT NULL,
    bid_quantity DECIMAL(10,2) NOT NULL,
    bid_status bid_status DEFAULT 'PENDING',
    bid_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================================================
-- ORDERS TABLE
-- =========================================================
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    crop_batch_id BIGINT NOT NULL REFERENCES crop_batches(id),
    farmer_id BIGINT NOT NULL REFERENCES users(id),
    retailer_id BIGINT NOT NULL REFERENCES users(id),
    bid_id BIGINT NOT NULL REFERENCES bids(id),
    final_amount DECIMAL(10,2) NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    order_status order_status DEFAULT 'PENDING',
    payment_status payment_status DEFAULT 'PENDING',
    delivery_address TEXT,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivery_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================================================
-- PAYMENTS TABLE
-- =========================================================
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(50),
    payment_status payment_status DEFAULT 'PENDING',
    transaction_id VARCHAR(100),
    payment_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================================================
-- NOTIFICATIONS TABLE
-- =========================================================
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================================================
-- AUDIT LOG TABLE
-- =========================================================
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    table_name VARCHAR(50) NOT NULL,
    record_id BIGINT NOT NULL,
    operation VARCHAR(10) NOT NULL CHECK (operation IN ('INSERT', 'UPDATE', 'DELETE')),
    old_values JSONB,
    new_values JSONB,
    user_id BIGINT,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE SET NULL
);

-- =========================================================
-- INDEXES FOR PERFORMANCE
-- =========================================================

-- Users indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_is_active ON users(is_active);

-- Crop batches indexes
CREATE INDEX idx_crop_farmer ON crop_batches(farmer_id);
CREATE INDEX idx_crop_status ON crop_batches(status);
CREATE INDEX idx_crop_location ON crop_batches(location);
CREATE INDEX idx_crop_created_at ON crop_batches(created_at DESC);
CREATE INDEX idx_crop_expiry ON crop_batches(expiry_date);
CREATE INDEX idx_crop_harvest ON crop_batches(harvest_date);

-- Bids indexes
CREATE INDEX idx_bids_crop_batch ON bids(crop_batch_id);
CREATE INDEX idx_bids_retailer ON bids(retailer_id);
CREATE INDEX idx_bids_status ON bids(bid_status);
CREATE INDEX idx_bids_amount ON bids(bid_amount DESC);
CREATE INDEX idx_bids_date ON bids(bid_date DESC);

-- Orders indexes
CREATE INDEX idx_orders_number ON orders(order_number);
CREATE INDEX idx_orders_crop ON orders(crop_batch_id);
CREATE INDEX idx_orders_farmer ON orders(farmer_id);
CREATE INDEX idx_orders_retailer ON orders(retailer_id);
CREATE INDEX idx_orders_status ON orders(order_status);
CREATE INDEX idx_orders_payment_status ON orders(payment_status);
CREATE INDEX idx_orders_date ON orders(order_date DESC);

-- Payments indexes
CREATE INDEX idx_payments_reference ON payments(payment_reference);
CREATE INDEX idx_payments_order ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(payment_status);
CREATE INDEX idx_payments_method ON payments(payment_method);
CREATE INDEX idx_payments_transaction ON payments(transaction_id);
CREATE INDEX idx_payments_date ON payments(payment_date DESC);

-- Audit logs indexes
CREATE INDEX idx_audit_table_record ON audit_logs(table_name, record_id);
CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_created_at ON audit_logs(created_at DESC);

-- =========================================================
-- UNIQUE CONSTRAINTS
-- =========================================================

-- Prevent duplicate pending bids from same retailer for same crop
CREATE UNIQUE INDEX idx_unique_pending_bid
    ON bids(crop_batch_id, retailer_id)
    WHERE bid_status = 'PENDING' AND is_active = true;

-- =========================================================
-- FUNCTIONS AND TRIGGERS
-- =========================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function to generate order number
CREATE OR REPLACE FUNCTION generate_order_number()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.order_number = 'ORD-' || TO_CHAR(CURRENT_DATE, 'YYYYMMDD') || '-' || LPAD(NEW.id::TEXT, 6, '0');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function to generate payment reference
CREATE OR REPLACE FUNCTION generate_payment_reference()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.payment_reference = 'PAY-' || TO_CHAR(CURRENT_DATE, 'YYYYMMDD') || '-' || LPAD(NEW.id::TEXT, 8, '0');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply triggers
CREATE TRIGGER trigger_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_crop_batches_updated_at
    BEFORE UPDATE ON crop_batches
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_bids_updated_at
    BEFORE UPDATE ON bids
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_orders_updated_at
    BEFORE UPDATE ON orders
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_payments_updated_at
    BEFORE UPDATE ON payments
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_generate_order_number
    BEFORE INSERT ON orders
    FOR EACH ROW
EXECUTE FUNCTION generate_order_number();

CREATE TRIGGER trigger_generate_payment_reference
    BEFORE INSERT ON payments
    FOR EACH ROW
EXECUTE FUNCTION generate_payment_reference();

-- =========================================================
-- SAMPLE DATA (OPTIONAL)
-- =========================================================

-- Insert admin user (password: admin123 - remember to hash in production)
INSERT INTO users (username, password, email, full_name, role, is_active, email_verified)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM7lbdxOuIsLbW.1J4W2', 'admin@agritrade.com', 'System Administrator', 'ADMIN', true, true);

-- =========================================================
-- VIEWS FOR REPORTING
-- =========================================================

CREATE VIEW active_crops AS
SELECT
    cb.*,
    u.full_name as farmer_name,
    u.phone_number as farmer_phone,
    COUNT(b.id) as bid_count,
    MAX(b.bid_amount) as highest_bid
FROM crop_batches cb
         JOIN users u ON cb.farmer_id = u.id
         LEFT JOIN bids b ON cb.id = b.crop_batch_id AND b.bid_status = 'PENDING'
WHERE cb.status = 'AVAILABLE' AND cb.is_active = true
GROUP BY cb.id, u.full_name, u.phone_number;

CREATE VIEW order_summary AS
SELECT
    o.*,
    cb.crop_name,
    f.full_name as farmer_name,
    r.full_name as retailer_name,
    p.payment_status as current_payment_status
FROM orders o
         JOIN crop_batches cb ON o.crop_batch_id = cb.id
         JOIN users f ON o.farmer_id = f.id
         JOIN users r ON o.retailer_id = r.id
         LEFT JOIN payments p ON o.id = p.order_id
WHERE o.is_active = true;

-- Grant permissions (adjust as needed)
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO agritrade_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO agritrade_user;
