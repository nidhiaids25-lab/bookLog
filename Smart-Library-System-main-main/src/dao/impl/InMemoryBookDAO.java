package dao.impl;

import Model.*;
import dao.BookDAO;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-Memory thread-safe implementation of BookDAO with fast HashMap lookup.
 * Includes PhysicalBooks, EBooks, and AudioBooks with genres and buyPrices.
 */
public class InMemoryBookDAO implements BookDAO {
    // HashMap lookup for O(1) retrieval by Book ID (Satisfies Day 3 HashMap lookup requirement)
    private final Map<String, Book> books = new ConcurrentHashMap<>();

    public InMemoryBookDAO() {
        seedSampleBooks();
    }

    private void seedSampleBooks() {
        // 1. Physical Books (Rent: ₹20/day, Shelf tracking)
        addBook(new PhysicalBook("B101", "Clean Code: A Handbook of Agile Software Craftsmanship", "Robert C. Martin", "Computer Science", 101, 599.0, true));
        addBook(new PhysicalBook("B102", "Effective Java (3rd Edition)", "Joshua Bloch", "Computer Science", 102, 649.0, true));
        addBook(new PhysicalBook("B103", "Design Patterns: Elements of Reusable Software", "Gang of Four", "Software Engineering", 103, 699.0, false));
        addBook(new PhysicalBook("B104", "Introduction to Algorithms (CLRS)", "Thomas H. Cormen", "Computer Science", 104, 899.0, true));
        addBook(new PhysicalBook("B105", "The Psychology of Money", "Morgan Housel", "Finance & Business", 105, 399.0, true));

        // 2. E-Books (Rent: ₹10/day, Digital PDF download)
        addBook(new EBook("E201", "Java: The Complete Reference (12th Ed)", "Herbert Schildt", "Computer Science", "https://library.edu/ebooks/java-complete-ref.pdf", 249.0, true));
        addBook(new EBook("E202", "Spring Boot in Action", "Craig Walls", "Software Engineering", "https://library.edu/ebooks/spring-boot-in-action.pdf", 299.0, true));
        addBook(new EBook("E203", "Database System Concepts", "Abraham Silberschatz", "Computer Science", "https://library.edu/ebooks/db-concepts.pdf", 320.0, true));
        addBook(new EBook("E204", "Deep Work: Rules for Focused Success", "Cal Newport", "Self-Help", "https://library.edu/ebooks/deep-work.pdf", 199.0, true));

        // 3. AudioBooks (New book type per Day 1 syllabus: Rent: ₹15/day, stream & narrator)
        addBook(new AudioBook("A301", "Atomic Habits: An Easy & Proven Way", "James Clear", "Self-Help", 320, "James Clear", "https://library.edu/audio/atomic-habits-preview.mp3", 349.0, true));
        addBook(new AudioBook("A302", "Sapiens: A Brief History of Humankind", "Yuval Noah Harari", "History & Science", 480, "Derek Perkins", "https://library.edu/audio/sapiens-preview.mp3", 399.0, true));
    }

    @Override
    public void addBook(Book book) {
        if (book != null && book.getBookId() != null) {
            books.put(book.getBookId().toUpperCase(), book);
        }
    }

    @Override
    public List<Book> getAllBooks() {
        return new ArrayList<>(books.values());
    }

    @Override
    public Book getBookById(String bookId) {
        if (bookId == null) return null;
        return books.get(bookId.toUpperCase());
    }

    @Override
    public boolean updateBookAuthor(String bookId, String newAuthor) {
        Book book = getBookById(bookId);
        if (book != null && newAuthor != null) {
            book.setAuthor(newAuthor);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateBook(Book book) {
        if (book != null && book.getBookId() != null && books.containsKey(book.getBookId().toUpperCase())) {
            books.put(book.getBookId().toUpperCase(), book);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteBook(String bookId) {
        if (bookId == null) return false;
        return books.remove(bookId.toUpperCase()) != null;
    }
}
