package service;

import Model.User;
import dao.DAOFactory;
import dao.UserDAO;

import java.util.List;
import java.util.stream.Collectors;

public class UserService {
    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = DAOFactory.getUserDAO();
    }

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Admin-exclusive Reader Account Generator.
     * Enforces Day 1 validation:
     * - Email format validation via regex
     * - Password length validation (>= 6 characters)
     */
    public User adminCreateReader(String customId, String name, String email, String password) {
        if (password == null || !User.isValidPassword(password)) {
            System.err.println("❌ Invalid password. Password must be at least 6 characters.");
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }

        if (email == null || !User.isValidEmail(email)) {
            System.err.println("❌ Invalid email format: " + email);
            throw new IllegalArgumentException("Invalid email format. E.g., reader@domain.com");
        }

        String userId = (customId != null && !customId.trim().isEmpty())
                ? customId.trim().toUpperCase()
                : "RDR-" + ((int) (Math.random() * 9000) + 1000);

        if (userDAO.getUserById(userId) != null) {
            throw new IllegalArgumentException("User ID '" + userId + "' already exists.");
        }

        String readerName = (name != null && !name.trim().isEmpty()) ? name.trim() : "Reader " + userId;
        User reader = new User(userId, readerName, email.trim(), password, "READER");
        boolean ok = userDAO.registerUser(reader);
        if (!ok) {
            throw new IllegalStateException("Failed to register reader in system.");
        }

        System.out.println("✅ Admin generated Reader account: ID=" + userId + ", Email=" + email);
        return reader;
    }

    public List<User> getAllReaders() {
        return userDAO.getAllUsers().stream()
                .filter(u -> "READER".equalsIgnoreCase(u.getRole()))
                .collect(Collectors.toList());
    }

    public boolean deleteReader(String userId) {
        return userDAO.deleteUser(userId);
    }

    // Account Register (Original signature preserved for backwards compatibility)
    public void register(String userId, String name, String password, String role) {
        registerUser(userId, name, password, role);
    }

    // Account Register with User return object for REST APIs
    public User registerUser(String userId, String name, String password, String role) {
        if (userId == null || userId.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            System.out.println("❌ User ID and password cannot be empty.");
            return null;
        }

        // Role check
        if (role == null || (!role.equalsIgnoreCase("READER") && !role.equalsIgnoreCase("SELLER") && !role.equalsIgnoreCase("ADMIN"))) {
            System.out.println("❌ Invalid Role! Choose either READER or ADMIN.");
            return null;
        }

        User newUser = new User(userId.trim(), name != null ? name.trim() : userId.trim(), password, role.toUpperCase());
        boolean isSuccess = userDAO.registerUser(newUser);

        if (isSuccess) {
            System.out.println("✅ Registration Successful as " + role.toUpperCase() + " for " + userId + "!");
            return newUser;
        } else {
            System.out.println("❌ Registration Failed. User ID " + userId + " might already exist.");
            return null;
        }
    }

    // Account Login
    public User login(String userId, String password) {
        if (userId == null || password == null) return null;
        User user = userDAO.loginUser(userId.trim(), password);
        if (user != null) {
            System.out.println("✅ Login Successful! Welcome " + user.getName() + " (" + user.getRole() + ")");
        } else {
            System.out.println("❌ Login Failed! Invalid User ID or Password.");
        }
        return user;
    }

    public User getUser(String userId) {
        return userDAO.getUserById(userId);
    }

    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }
}