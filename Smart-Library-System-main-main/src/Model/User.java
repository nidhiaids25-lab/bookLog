package Model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class User {
    private String userId;
    private String name;
    private String email;
    private String password;
    private String role; // "ADMIN" or "READER"
    private boolean createdByAdmin = true;
    private String createdAt;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    public User(String userId, String name, String password, String role) {
        this.userId = userId;
        this.name = name;
        this.email = userId.toLowerCase() + "@library.edu";
        this.password = password;
        this.role = normalizeRole(role);
        this.createdByAdmin = true;
        this.createdAt = java.time.LocalDate.now().toString();
    }

    public User(String userId, String name, String email, String password, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = normalizeRole(role);
        this.createdByAdmin = true;
        this.createdAt = java.time.LocalDate.now().toString();
    }

    private String normalizeRole(String r) {
        if (r == null) return "READER";
        if ("SELLER".equalsIgnoreCase(r) || "ADMIN".equalsIgnoreCase(r)) return "ADMIN";
        return "READER";
    }

    // Validation Rules
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        if (password == null) return false;
        return password.trim().length() >= 6;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = normalizeRole(role); }

    public boolean isCreatedByAdmin() { return createdByAdmin; }
    public void setCreatedByAdmin(boolean createdByAdmin) { this.createdByAdmin = createdByAdmin; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public Map<String, Object> toSafeMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("userId", userId);
        map.put("name", name);
        map.put("email", email);
        map.put("role", role);
        map.put("createdAt", createdAt != null ? createdAt : "");
        return map;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("userId", userId);
        map.put("name", name);
        map.put("email", email);
        map.put("password", password);
        map.put("role", role);
        map.put("createdByAdmin", createdByAdmin);
        map.put("createdAt", createdAt != null ? createdAt : "");
        return map;
    }
}