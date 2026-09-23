package dao.impl;

import Model.Transaction;
import dao.TransactionDAO;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL/JDBC implementation of TransactionDAO.
 */
public class DatabaseTransactionDAO implements TransactionDAO {

    @Override
    public void addTransaction(Transaction tx) {
        String sql = "INSERT INTO transactions (transaction_id, user_id, book_id, issue_date, due_date, return_date, rental_price) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tx.getTransactionId());
            stmt.setString(2, tx.getUserId());
            stmt.setString(3, tx.getBookId());
            stmt.setString(4, tx.getIssueDate());
            stmt.setString(5, tx.getDueDate());
            stmt.setString(6, tx.getReturnDate());
            stmt.setDouble(7, tx.getRentalPrice());
            stmt.executeUpdate();
            System.out.println("✅ Transaction saved to database: " + tx.getTransactionId());
        } catch (SQLException e) {
            System.err.println("❌ Database error saving transaction: " + e.getMessage());
        }
    }

    @Override
    public Transaction getTransactionById(String transactionId) {
        String sql = "SELECT t.*, b.title as book_title FROM transactions t LEFT JOIN books b ON t.book_id = b.book_id WHERE t.transaction_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, transactionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Database error getting transaction: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, b.title as book_title FROM transactions t LEFT JOIN books b ON t.book_id = b.book_id ORDER BY t.issue_date DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Database error listing transactions: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Transaction> getTransactionsByUserId(String userId) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, b.title as book_title FROM transactions t LEFT JOIN books b ON t.book_id = b.book_id WHERE t.user_id = ? ORDER BY t.issue_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Database error listing user transactions: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean updateTransaction(Transaction tx) {
        String sql = "UPDATE transactions SET return_date = ?, rental_price = ? WHERE transaction_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tx.getReturnDate());
            stmt.setDouble(2, tx.getRentalPrice());
            stmt.setString(3, tx.getTransactionId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Database error updating transaction: " + e.getMessage());
            return false;
        }
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        String retDate = rs.getString("return_date");
        String status = (retDate != null && !retDate.isEmpty()) ? "RETURNED" : "BORROWED";
        return new Transaction(
            rs.getString("transaction_id"),
            rs.getString("user_id"),
            rs.getString("book_id"),
            rs.getString("book_title"),
            rs.getString("issue_date"),
            rs.getString("due_date"),
            retDate,
            rs.getDouble("rental_price"),
            status
        );
    }
}
