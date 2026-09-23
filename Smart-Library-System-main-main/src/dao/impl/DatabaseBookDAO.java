package dao.impl;

import Model.*;
import dao.BookDAO;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL/JDBC implementation of BookDAO.
 * Supports PhysicalBook, EBook, and AudioBook with complete SQL queries.
 */
public class DatabaseBookDAO implements BookDAO {

    @Override
    public void addBook(Book book) {
        String sql = "INSERT INTO books (book_id, title, author, genre, book_type, shelf_number, download_link, audio_duration, narrator, buy_price) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE title=VALUES(title), author=VALUES(author), genre=VALUES(genre), buy_price=VALUES(buy_price)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getBookId());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setString(4, book.getGenre());
            stmt.setString(5, book.getBookType());

            if (book instanceof PhysicalBook) {
                stmt.setInt(6, ((PhysicalBook) book).getShelfNumber());
                stmt.setNull(7, Types.VARCHAR);
                stmt.setNull(8, Types.INTEGER);
                stmt.setNull(9, Types.VARCHAR);
            } else if (book instanceof EBook) {
                stmt.setNull(6, Types.INTEGER);
                stmt.setString(7, ((EBook) book).getDownloadLink());
                stmt.setNull(8, Types.INTEGER);
                stmt.setNull(9, Types.VARCHAR);
            } else if (book instanceof AudioBook) {
                stmt.setNull(6, Types.INTEGER);
                stmt.setString(7, ((AudioBook) book).getAudioStreamUrl());
                stmt.setInt(8, ((AudioBook) book).getAudioDurationMinutes());
                stmt.setString(9, ((AudioBook) book).getNarrator());
            } else {
                stmt.setNull(6, Types.INTEGER);
                stmt.setNull(7, Types.VARCHAR);
                stmt.setNull(8, Types.INTEGER);
                stmt.setNull(9, Types.VARCHAR);
            }
            stmt.setDouble(10, book.getBuyPrice());

            stmt.executeUpdate();
            System.out.println("✅ Book saved to database: " + book.getTitle());
        } catch (SQLException e) {
            System.err.println("❌ Database error adding book: " + e.getMessage());
        }
    }

    @Override
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                books.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Database error getting books: " + e.getMessage());
        }
        return books;
    }

    @Override
    public Book getBookById(String bookId) {
        String sql = "SELECT * FROM books WHERE book_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Database error getting book: " + e.getMessage());
        }
        return null;
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        String id = rs.getString("book_id");
        String title = rs.getString("title");
        String author = rs.getString("author");
        String genre = rs.getString("genre");
        String type = rs.getString("book_type");
        double buyPrice = rs.getDouble("buy_price");
        if (buyPrice <= 0) buyPrice = 399.0;

        if ("EBOOK".equalsIgnoreCase(type)) {
            String link = rs.getString("download_link");
            return new EBook(id, title, author, genre != null ? genre : "Computer Science", link != null ? link : "", buyPrice, true);
        } else if ("AUDIOBOOK".equalsIgnoreCase(type)) {
            String link = rs.getString("download_link");
            int duration = rs.getInt("audio_duration");
            String narrator = rs.getString("narrator");
            return new AudioBook(id, title, author, genre != null ? genre : "General", duration, narrator != null ? narrator : "Narrator", link != null ? link : "", buyPrice, true);
        } else {
            int shelf = rs.getInt("shelf_number");
            return new PhysicalBook(id, title, author, genre != null ? genre : "Computer Science", shelf > 0 ? shelf : 101, buyPrice, true);
        }
    }

    @Override
    public boolean updateBookAuthor(String bookId, String newAuthor) {
        String sql = "UPDATE books SET author = ? WHERE book_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newAuthor);
            stmt.setString(2, bookId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Database error updating book author: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateBook(Book book) {
        String sql = "UPDATE books SET title = ?, author = ?, genre = ?, buy_price = ? WHERE book_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getGenre());
            stmt.setDouble(4, book.getBuyPrice());
            stmt.setString(5, book.getBookId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Database error updating book: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteBook(String bookId) {
        String sql = "DELETE FROM books WHERE book_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bookId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Database error deleting book: " + e.getMessage());
            return false;
        }
    }
}
