package com.chatapp.server.service;

import com.chatapp.common.model.User;
import com.chatapp.common.util.PasswordUtil;
import com.chatapp.server.database.dao.UserDAO;
import com.chatapp.server.util.Logger;

import java.sql.SQLException;
import java.util.UUID;

public class AuthService {
    private final UserDAO userDAO;
    private final Logger logger = Logger.getInstance();

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Register new user
     */
    public User register(String username, String email, String password, String fullName) throws Exception {
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            throw new Exception("Tên đăng nhập không được để trống");
        }
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new Exception("Email không hợp lệ");
        }
        if (password == null || password.length() < 6) {
            throw new Exception("Mật khẩu phải có ít nhất 6 ký tự");
        }

        // Check if username exists
        if (userDAO.existsByUsername(username)) {
            throw new Exception("Tên đăng nhập đã tồn tại");
        }

        // Check if email exists
        if (userDAO.existsByEmail(email)) {
            throw new Exception("Email đã được sử dụng");
        }

        // Hash password
        String passwordHash = PasswordUtil.hashPassword(password);

        // Create user
        User user = new User(username, email, passwordHash);
        user.setFullName(fullName);

        // Save to database
        userDAO.insert(user);

        logger.info("New user registered: " + username);
        return user;
    }

    /**
     * Login user
     */
    public User login(String username, String password) throws Exception {
        if (username == null || username.trim().isEmpty()) {
            throw new Exception("Tên đăng nhập không được để trống");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new Exception("Mật khẩu không được để trống");
        }

        // Get user from database
        User user = userDAO.findByUsername(username);
        if (user == null) {
            throw new Exception("Tên đăng nhập hoặc mật khẩu không đúng");
        }

        // Check password
        if (!PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            throw new Exception("Tên đăng nhập hoặc mật khẩu không đúng");
        }

        // Check if account is active
        if (!user.isActive()) {
            throw new Exception("Tài khoản đã bị khóa");
        }

        logger.info("User logged in: " + username);
        return user;
    }

    /**
     * Logout user
     */
    public void logout(Long userId) throws SQLException {
        userDAO.updateStatus(userId, User.UserStatus.OFFLINE, null, null);
        logger.info("User logged out: " + userId);
    }

    /**
     * Update user online status
     */
    public void updateOnlineStatus(Long userId, String ipAddress, Integer port) throws SQLException {
        userDAO.updateStatus(userId, User.UserStatus.ONLINE, ipAddress, port);
    }
}