CREATE TABLE inventory (
    inventory_id VARCHAR(50) PRIMARY KEY,
    library_id VARCHAR(50) NOT NULL,
    book_id VARCHAR(50) NOT NULL,
    total_copies INT DEFAULT 1,
    available_copies INT DEFAULT 1,
    FOREIGN KEY (library_id) REFERENCES libraries(library_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE
);