package dao.impl;

import Model.Reservation;
import dao.ReservationDAO;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * MySQL/JDBC implementation of ReservationDAO.
 */
public class DatabaseReservationDAO implements ReservationDAO {

    @Override
    public void addReservation(Reservation res) {
        String sql = "INSERT INTO reservations (reservation_id, user_id, book_id, reservation_date, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, res.getReservationId());
            stmt.setString(2, res.getUserId());
            stmt.setString(3, res.getBookId());
            stmt.setString(4, res.getReservationDate());
            stmt.setString(5, res.getStatus());
            stmt.executeUpdate();
            System.out.println("✅ Reservation saved to database: " + res.getReservationId());
        } catch (SQLException e) {
            System.err.println("❌ Database error saving reservation: " + e.getMessage());
        }
    }

    @Override
    public Reservation getReservationById(String reservationId) {
        String sql = "SELECT r.*, b.title as book_title FROM reservations r LEFT JOIN books b ON r.book_id = b.book_id WHERE r.reservation_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reservationId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Database error getting reservation: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Reservation> getAllReservations() {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT r.*, b.title as book_title FROM reservations r LEFT JOIN books b ON r.book_id = b.book_id ORDER BY r.reservation_date DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Database error listing reservations: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Reservation> getReservationsByUserId(String userId) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT r.*, b.title as book_title FROM reservations r LEFT JOIN books b ON r.book_id = b.book_id WHERE r.user_id = ? ORDER BY r.reservation_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Database error listing user reservations: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Reservation> getReservationsByBookId(String bookId) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT r.*, b.title as book_title FROM reservations r LEFT JOIN books b ON r.book_id = b.book_id WHERE r.book_id = ? AND r.status = 'PENDING'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Database error listing book reservations: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean updateReservation(Reservation res) {
        String sql = "UPDATE reservations SET status = ? WHERE reservation_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, res.getStatus());
            stmt.setString(2, res.getReservationId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Database error updating reservation: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteReservation(String reservationId) {
        String sql = "DELETE FROM reservations WHERE reservation_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reservationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Database error deleting reservation: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Queue<Reservation> getReservationQueue(String bookId) {
        List<Reservation> list = getReservationsByBookId(bookId);
        return new LinkedList<>(list);
    }

    @Override
    public Reservation pollNextInQueue(String bookId) {
        List<Reservation> pending = getReservationsByBookId(bookId);
        if (!pending.isEmpty()) {
            Reservation next = pending.get(0);
            next.setStatus("READY");
            updateReservation(next);
            return next;
        }
        return null;
    }

    @Override
    public int getQueuePosition(String reservationId) {
        Reservation res = getReservationById(reservationId);
        if (res == null) return -1;
        List<Reservation> pending = getReservationsByBookId(res.getBookId());
        for (int i = 0; i < pending.size(); i++) {
            if (pending.get(i).getReservationId().equalsIgnoreCase(reservationId)) {
                return i + 1;
            }
        }
        return -1;
    }

    private Reservation mapRow(ResultSet rs) throws SQLException {
        return new Reservation(
            rs.getString("reservation_id"),
            rs.getString("user_id"),
            rs.getString("book_id"),
            rs.getString("book_title"),
            rs.getString("reservation_date"),
            rs.getString("status")
        );
    }
}
