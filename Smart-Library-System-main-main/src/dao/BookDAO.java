package dao;

import Model.Book;
import java.util.List;

/**
 * Interface defining standard Book Data Access operations.
 * Allows switching between In-Memory storage and MySQL/Database storage easily.
 */
public interface BookDAO {
    void addBook(Book book);
    List<Book> getAllBooks();
    Book getBookById(String bookId);
    boolean updateBookAuthor(String bookId, String newAuthor);
    boolean updateBook(Book book);
    boolean deleteBook(String bookId);
}