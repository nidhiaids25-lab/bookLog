package service;

import Model.Book;
import dao.BookDAO;
import dao.DAOFactory;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BookService {
    private final BookDAO bookDAO;

    public BookService() {
        this.bookDAO = DAOFactory.getBookDAO();
    }

    public BookService(BookDAO bookDAO) {
        this.bookDAO = bookDAO;
    }

    public void addBook(Book book) {
        if (book != null && book.getBookId() != null) {
            bookDAO.addBook(book);
        }
    }

    public List<Book> getAllBooks() {
        return bookDAO.getAllBooks();
    }

    public Book getBookById(String bookId) {
        return bookDAO.getBookById(bookId);
    }

    public List<Book> searchBooks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllBooks();
        }
        String q = query.trim().toLowerCase();
        return bookDAO.getAllBooks().stream()
                .filter(b -> (b.getTitle() != null && b.getTitle().toLowerCase().contains(q))
                          || (b.getAuthor() != null && b.getAuthor().toLowerCase().contains(q))
                          || (b.getBookId() != null && b.getBookId().toLowerCase().contains(q)))
                .collect(Collectors.toList());
    }

    public List<Book> filterByType(String type) {
        if (type == null || "ALL".equalsIgnoreCase(type)) {
            return getAllBooks();
        }
        return bookDAO.getAllBooks().stream()
                .filter(b -> type.equalsIgnoreCase(b.getBookType()))
                .collect(Collectors.toList());
    }

    /**
     * Search, filter, and sort in a single unified operation.
     */
    public List<Book> queryBooks(String query, String type, String sort) {
        List<Book> list = getAllBooks();

        // 1. Search text filter (title, author, genre, bookId)
        if (query != null && !query.trim().isEmpty()) {
            String q = query.trim().toLowerCase();
            list = list.stream()
                    .filter(b -> (b.getTitle() != null && b.getTitle().toLowerCase().contains(q))
                              || (b.getAuthor() != null && b.getAuthor().toLowerCase().contains(q))
                              || (b.getGenre() != null && b.getGenre().toLowerCase().contains(q))
                              || (b.getBookId() != null && b.getBookId().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }

        // 2. Type filter
        if (type != null && !"ALL".equalsIgnoreCase(type)) {
            if ("AVAILABLE".equalsIgnoreCase(type)) {
                list = list.stream().filter(Book::isAvailable).collect(Collectors.toList());
            } else if ("LOANED".equalsIgnoreCase(type)) {
                list = list.stream().filter(b -> !b.isAvailable()).collect(Collectors.toList());
            } else {
                list = list.stream()
                        .filter(b -> type.equalsIgnoreCase(b.getBookType()))
                        .collect(Collectors.toList());
            }
        }

        // 3. Sorting with Comparators (Day 3: Price / Title sorting)
        if ("title".equalsIgnoreCase(sort) || "title_asc".equalsIgnoreCase(sort)) {
            list.sort(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER));
        } else if ("title_desc".equalsIgnoreCase(sort)) {
            list.sort(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER).reversed());
        } else if ("author".equalsIgnoreCase(sort)) {
            list.sort(Comparator.comparing(Book::getAuthor, String.CASE_INSENSITIVE_ORDER));
        } else if ("price_asc".equalsIgnoreCase(sort) || "buy_price_asc".equalsIgnoreCase(sort)) {
            list.sort(Comparator.comparingDouble(Book::getBuyPrice));
        } else if ("price_desc".equalsIgnoreCase(sort) || "buy_price_desc".equalsIgnoreCase(sort)) {
            list.sort((b1, b2) -> Double.compare(b2.getBuyPrice(), b1.getBuyPrice()));
        } else if ("rent_asc".equalsIgnoreCase(sort)) {
            list.sort(Comparator.comparingDouble(b -> b.getRentalPrice(1)));
        } else if ("rent_desc".equalsIgnoreCase(sort)) {
            list.sort((b1, b2) -> Double.compare(b2.getRentalPrice(1), b1.getRentalPrice(1)));
        }

        return list;
    }

    public boolean updateBookAuthor(String bookId, String newAuthor) {
        return bookDAO.updateBookAuthor(bookId, newAuthor);
    }

    public boolean updateBook(Book book) {
        return bookDAO.updateBook(book);
    }

    public boolean deleteBook(String bookId) {
        return bookDAO.deleteBook(bookId);
    }

    /**
     * Polymorphism in action:
     * PhysicalBook charges ₹20/day, EBook charges ₹10/day.
     */
    public double calculateRentalPrice(String bookId, int days) {
        Book book = getBookById(bookId);
        if (book == null) return 0.0;
        return book.getRentalPrice(Math.max(1, days));
    }
}
