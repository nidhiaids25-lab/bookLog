package dao.impl;

import Model.User;
import dao.UserDAO;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL/JDBC implementation of UserDAO.
 */
public class DatabaseUserDAO implements UserDAO {

    @Override
    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (user_id, name, email, password, role) VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE name=VALUES(name), email=VALUES(email), password=VALUES(password)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUserId());
            stmt.setString(2, user.getName());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPassword());
            stmt.setString(5, user.getRole());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Database error registering user: " + e.getMessage());
            return false;
        }
    }

    @Override
    public User loginUser(String userId, String password) {
        String sql = "SELECT * FROM users WHERE (user_id = ? OR email = ?) AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, userId);
            stmt.setString(3, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String email = rs.getString("email");
                    return new User(
                        rs.getString("user_id"),
                        rs.getString("name"),
                        email != null ? email : rs.getString("user_id") + "@library.edu",
                        rs.getString("password"),
                        rs.getString("role")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Database error logging in: " + e.getMessage());
        }
        return null;
    }

    @Override
    public User getUserById(String userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String email = rs.getString("email");
                    return new User(
                        rs.getString("user_id"),
                        rs.getString("name"),
                        email != null ? email : rs.getString("user_id") + "@library.edu",
                        rs.getString("password"),
                        rs.getString("role")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Database error getting user: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String email = rs.getString("email");
                list.add(new User(
                    rs.getString("user_id"),
                    rs.getString("name"),
                    email != null ? email : rs.getString("user_id") + "@library.edu",
                    rs.getString("password"),
                    rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            System.err.println("❌ Database error listing users: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean deleteUser(String userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Database error deleting user: " + e.getMessage());
            return false;
        }
    }
}
