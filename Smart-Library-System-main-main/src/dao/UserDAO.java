package dao;

import Model.User;
import java.util.List;

/**
 * Interface defining User Data Access operations.
 */
public interface UserDAO {
    boolean registerUser(User user);
    User loginUser(String userId, String password);
    User getUserById(String userId);
    List<User> getAllUsers();
    boolean deleteUser(String userId);
}