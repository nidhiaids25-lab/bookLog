-- =====================================================================
-- Smart Library System - Unified Database Schema & Initial Data
-- =====================================================================

CREATE DATABASE IF NOT EXISTS library_db;
USE library_db;

-- 1. Libraries Table
CREATE TABLE IF NOT EXISTS libraries (
    library_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(200) NOT NULL
);

-- 2. Users Table
CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL -- 'READER' or 'SELLER'
);

-- 3. Books Table (Supports Physical and E-Book polymorphic types)
CREATE TABLE IF NOT EXISTS books (
    book_id VARCHAR(50) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100) NOT NULL,
    book_type VARCHAR(20) NOT NULL, -- 'PHYSICAL' or 'EBOOK'
    shelf_number INT,               -- For PhysicalBook
    download_link VARCHAR(255)      -- For EBook
);

-- 4. Inventory Table
CREATE TABLE IF NOT EXISTS inventory (
    inventory_id VARCHAR(50) PRIMARY KEY,
    library_id VARCHAR(50) NOT NULL,
    book_id VARCHAR(50) NOT NULL,
    total_copies INT DEFAULT 1,
    available_copies INT DEFAULT 1,
    FOREIGN KEY (library_id) REFERENCES libraries(library_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE
);

-- 5. Transactions Table (Borrow & Return Records)
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    book_id VARCHAR(50) NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    rental_price DECIMAL(10, 2),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE
);

-- 6. Reservations Table
CREATE TABLE IF NOT EXISTS reservations (
    reservation_id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    book_id VARCHAR(50) NOT NULL,
    reservation_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING', -- 'PENDING', 'FULFILLED', 'CANCELLED'
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE
);

-- =====================================================================
-- Initial Seed Data
-- =====================================================================

INSERT IGNORE INTO libraries (library_id, name, location) VALUES
('LIB-001', 'Central Smart Library', 'Main Campus, Building A');

INSERT IGNORE INTO users (user_id, name, password, role) VALUES
('U101', 'Nidhi', 'pass123', 'READER'),
('U102', 'Rahul', 'seller123', 'SELLER'),
('U103', 'Ken', 'ken123', 'READER'),
('U104', 'Admin', 'admin123', 'SELLER');

INSERT IGNORE INTO books (book_id, title, author, book_type, shelf_number, download_link) VALUES
('B101', 'Clean Code: A Handbook of Agile Software Craftsmanship', 'Robert C. Martin', 'PHYSICAL', 101, NULL),
('B102', 'Effective Java (3rd Edition)', 'Joshua Bloch', 'PHYSICAL', 102, NULL),
('B103', 'Design Patterns: Elements of Reusable Object-Oriented Software', 'Gang of Four', 'PHYSICAL', 103, NULL),
('B104', 'Introduction to Algorithms (CLRS)', 'Thomas H. Cormen', 'PHYSICAL', 104, NULL),
('E201', 'Java: The Complete Reference (12th Ed)', 'Herbert Schildt', 'EBOOK', NULL, 'https://library.edu/ebooks/java-complete-ref.pdf'),
('E202', 'Spring Boot in Action', 'Craig Walls', 'EBOOK', NULL, 'https://library.edu/ebooks/spring-boot-in-action.pdf'),
('E203', 'Database System Concepts', 'Abraham Silberschatz', 'EBOOK', NULL, 'https://library.edu/ebooks/db-concepts.pdf'),
('E204', 'Modern Operating Systems', 'Andrew S. Tanenbaum', 'EBOOK', NULL, 'https://library.edu/ebooks/modern-os.pdf');

INSERT IGNORE INTO transactions (transaction_id, user_id, book_id, issue_date, due_date, return_date, rental_price) VALUES
('TX-1001', 'U101', 'B103', '2026-09-15', '2026-09-29', NULL, 280.00),
('TX-1002', 'U103', 'B101', '2026-09-01', '2026-09-15', '2026-09-14', 260.00);

INSERT IGNORE INTO reservations (reservation_id, user_id, book_id, reservation_date, status) VALUES
('RES-501', 'U103', 'B103', '2026-09-18', 'PENDING');
