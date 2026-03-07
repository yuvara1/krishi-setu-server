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
-- CREATE ENUM TYPES
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
                       username VARCHAR(50) UNIQUE NOT NULL CHECK (length(trim(username)) >= 3),
                       password VARCHAR(255) NOT NULL CHECK (length(password) >= 8), -- Allow shorter for testing
                       email VARCHAR(100) UNIQUE NOT NULL CHECK (email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
                       full_name VARCHAR(100) NOT NULL CHECK (length(trim(full_name)) >= 2),
                       phone_number VARCHAR(15) CHECK (phone_number ~ '^\+?[1-9]\d{1,14}$'),
                       role user_role NOT NULL,
                       address TEXT,
                       is_active BOOLEAN DEFAULT true,
                       email_verified BOOLEAN DEFAULT false,
                       phone_verified BOOLEAN DEFAULT false,
                       created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                       last_login TIMESTAMP WITH TIME ZONE,
                       CONSTRAINT users_username_length CHECK (length(trim(username)) BETWEEN 3 AND 50),
                       CONSTRAINT users_email_length CHECK (length(trim(email)) BETWEEN 5 AND 100)
);

-- =========================================================
-- CROP BATCHES TABLE
-- =========================================================
CREATE TABLE crop_batches (
                              id BIGSERIAL PRIMARY KEY,
                              farmer_id BIGINT NOT NULL,
                              crop_name VARCHAR(100) NOT NULL CHECK (length(trim(crop_name)) >= 2),
                              crop_type VARCHAR(50) CHECK (length(trim(crop_type)) >= 2),
                              quantity DECIMAL(12,3) NOT NULL CHECK (quantity > 0),
                              base_price DECIMAL(12,2) NOT NULL CHECK (base_price >= 0),
                              harvest_date DATE,
                              expiry_date DATE,
                              description TEXT CHECK (length(trim(description)) <= 2000),
                              status crop_status DEFAULT 'AVAILABLE',
                              image_url VARCHAR(500),
                              location VARCHAR(200) NOT NULL CHECK (length(trim(location)) >= 2),
                              is_organic BOOLEAN DEFAULT false,
                              unit VARCHAR(20) DEFAULT 'kg' CHECK (unit IN ('kg', 'quintal', 'ton', 'piece', 'dozen')),
                              quality_grade VARCHAR(10) CHECK (quality_grade IN ('A+', 'A', 'B+', 'B', 'C')),
                              minimum_bid_amount DECIMAL(12,2) CHECK (minimum_bid_amount >= base_price),
                              is_active BOOLEAN DEFAULT true,
                              created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT fk_crop_farmer
                                  FOREIGN KEY (farmer_id)
                                      REFERENCES users(id)
                                      ON DELETE CASCADE,
                              CONSTRAINT crop_dates_valid
                                  CHECK (expiry_date IS NULL OR harvest_date IS NULL OR expiry_date > harvest_date)
);

-- =========================================================
-- BIDS TABLE
-- =========================================================
CREATE TABLE bids (
                      id BIGSERIAL PRIMARY KEY,
                      crop_batch_id BIGINT NOT NULL,
                      retailer_id BIGINT NOT NULL,
                      bid_amount DECIMAL(12,2) NOT NULL CHECK (bid_amount > 0),
                      bid_quantity DECIMAL(12,3) NOT NULL CHECK (bid_quantity > 0),
                      bid_status bid_status DEFAULT 'PENDING',
                      bid_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                      remarks TEXT CHECK (length(trim(remarks)) <= 500),
                      is_active BOOLEAN DEFAULT true,
                      expires_at TIMESTAMP WITH TIME ZONE,
                      created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                      CONSTRAINT fk_bid_crop
                          FOREIGN KEY (crop_batch_id)
                              REFERENCES crop_batches(id)
                              ON DELETE CASCADE,
                      CONSTRAINT fk_bid_retailer
                          FOREIGN KEY (retailer_id)
                              REFERENCES users(id)
                              ON DELETE CASCADE,
                      CONSTRAINT bid_expiry_valid
                          CHECK (expires_at IS NULL OR expires_at > bid_date)
);

-- =========================================================
-- ORDERS TABLE
-- =========================================================
CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,
                        order_number VARCHAR(20) UNIQUE,
                        crop_batch_id BIGINT NOT NULL,
                        farmer_id BIGINT NOT NULL,
                        retailer_id BIGINT NOT NULL,
                        bid_id BIGINT,
                        final_amount DECIMAL(12,2) NOT NULL CHECK (final_amount >= 0),
                        quantity DECIMAL(12,3) NOT NULL CHECK (quantity > 0),
                        order_status order_status DEFAULT 'PENDING',
                        payment_status payment_status DEFAULT 'PENDING',
                        delivery_address TEXT NOT NULL CHECK (length(trim(delivery_address)) >= 10),
                        delivery_contact VARCHAR(15) CHECK (delivery_contact ~ '^\+?[1-9]\d{1,14}$'),
                        order_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                        expected_delivery_date DATE,
                        actual_delivery_date TIMESTAMP WITH TIME ZONE,
                        cancellation_reason TEXT,
                        is_active BOOLEAN DEFAULT true,
                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT fk_order_crop
                            FOREIGN KEY (crop_batch_id)
                                REFERENCES crop_batches(id)
                                ON DELETE RESTRICT,
                        CONSTRAINT fk_order_farmer
                            FOREIGN KEY (farmer_id)
                                REFERENCES users(id)
                                ON DELETE RESTRICT,
                        CONSTRAINT fk_order_retailer
                            FOREIGN KEY (retailer_id)
                                REFERENCES users(id)
                                ON DELETE RESTRICT,
                        CONSTRAINT fk_order_bid
                            FOREIGN KEY (bid_id)
                                REFERENCES bids(id)
                                ON DELETE SET NULL
);

-- =========================================================
-- PAYMENTS TABLE
-- =========================================================
CREATE TABLE payments (
                          id BIGSERIAL PRIMARY KEY,
                          payment_reference VARCHAR(50) UNIQUE,
                          order_id BIGINT NOT NULL,
                          amount DECIMAL(12,2) NOT NULL CHECK (amount >= 0),
                          payment_method VARCHAR(50) NOT NULL,
                          payment_status payment_status DEFAULT 'PENDING',
                          transaction_id VARCHAR(100),
                          gateway_response JSONB,
                          payment_date TIMESTAMP WITH TIME ZONE,
                          failure_reason TEXT,
                          refund_amount DECIMAL(12,2) DEFAULT 0 CHECK (refund_amount >= 0),
                          refund_date TIMESTAMP WITH TIME ZONE,
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT fk_payment_order
                              FOREIGN KEY (order_id)
                                  REFERENCES orders(id)
                                  ON DELETE CASCADE,
                          CONSTRAINT refund_valid
                              CHECK (refund_amount <= amount)
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
