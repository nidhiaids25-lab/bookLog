CREATE TABLE books (
    book_id VARCHAR(50) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100) NOT NULL,
    book_type VARCHAR(20) NOT NULL, -- 'PHYSICAL' ya 'EBOOK'
    shelf_number INT,               -- PhysicalBook ke liye (Optional)
    download_link VARCHAR(255)      -- EBook ke liye (Optional)
);
