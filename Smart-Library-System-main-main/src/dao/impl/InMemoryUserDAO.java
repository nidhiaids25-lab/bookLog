package dao.impl;

import Model.User;
import dao.UserDAO;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-Memory UserDAO.
 * Per specification: ONLY the Admin account is pre-configured.
 * Readers must be generated and issued by the Admin via the Admin Panel.
 */
public class InMemoryUserDAO implements UserDAO {
    private final Map<String, User> users = new ConcurrentHashMap<>();

    public InMemoryUserDAO() {
        seedAdminOnly();
    }

    private void seedAdminOnly() {
        // ONLY Admin account pre-seeded as requested
        User admin = new User("admin", "System Administrator", "admin@library.edu", "admin123", "ADMIN");
        registerUser(admin);

        // Also add ADMIN-001 alias for convenience
        User adminAlias = new User("ADMIN-001", "System Administrator", "admin@library.edu", "admin123", "ADMIN");
        registerUser(adminAlias);
    }

    @Override
    public boolean registerUser(User user) {
        if (user == null || user.getUserId() == null) return false;
        String key = user.getUserId().toUpperCase().trim();
        if (users.containsKey(key)) {
            return false; // User already exists
        }
        users.put(key, user);
        return true;
    }

    @Override
    public User loginUser(String userId, String password) {
        if (userId == null || password == null) return null;
        User user = users.get(userId.toUpperCase().trim());
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    @Override
    public User getUserById(String userId) {
        if (userId == null) return null;
        return users.get(userId.toUpperCase().trim());
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public boolean deleteUser(String userId) {
        if (userId == null) return false;
        String key = userId.toUpperCase().trim();
        // Prevent deleting root admin
        if ("ADMIN".equals(key) || "ADMIN-001".equals(key)) return false;
        return users.remove(key) != null;
    }
}
