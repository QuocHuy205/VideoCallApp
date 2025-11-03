package com.chatapp.server.service;

import com.chatapp.common.model.User;
import com.chatapp.common.protocol.Packet;
import com.chatapp.common.protocol.PacketBuilder;
import com.chatapp.common.protocol.MessageType;
import com.chatapp.server.database.dao.UserDAO;
import com.chatapp.server.util.Logger;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Map;

/**
 * Service xử lý authentication (login, register, logout)
 */
public class AuthService {
    private static AuthService instance;
    private final UserDAO userDAO;
    private final Logger logger = Logger.getInstance();

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public static AuthService getInstance() {
        if (instance == null) {
            synchronized (AuthService.class) {
                if (instance == null) {
                    instance = new AuthService();
                }
            }
        }
        return instance;
    }

    public Packet handleLogin(Packet request) {
        try {
            Map<String, Object> data = request.getData();
            String username = (String) data.get("username");
            String password = (String) data.get("password");

            // Validate input
            if (username == null || username.trim().isEmpty() ||
                    password == null || password.trim().isEmpty()) {
                return PacketBuilder.create(MessageType.LOGIN_RESPONSE)
                        .error("Username and password are required")
                        .build();
            }

            // Tìm user
            User user = userDAO.findByUsername(username.trim());
            if (user == null) {
                logger.warn("Login failed: User not found - " + username);
                return PacketBuilder.create(MessageType.LOGIN_RESPONSE)
                        .error("Invalid username or password")
                        .build();
            }

            // Kiểm tra password
            if (!BCrypt.checkpw(password, user.getPasswordHash())) {
                logger.warn("Login failed: Wrong password - " + username);
                return PacketBuilder.create(MessageType.LOGIN_RESPONSE)
                        .error("Invalid username or password")
                        .build();
            }

            // Cập nhật status thành ONLINE
            userDAO.updateStatus(user.getId(), User.UserStatus.ONLINE, null, null);
            user.setStatusType(User.UserStatus.ONLINE);

            // Không trả về password hash
            user.setPasswordHash(null);

            logger.info("User logged in successfully: " + username);

            return PacketBuilder.create(MessageType.LOGIN_RESPONSE)
                    .success(true)
                    .put("message", "Login successful")
                    .put("userId", user.getId())
                    .put("user", user)
                    .build();

        } catch (SQLException e) {
            logger.error("Database error during login: " + e.getMessage(), e);
            return PacketBuilder.create(MessageType.LOGIN_RESPONSE)
                    .error("Database error: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            logger.error("Error during login: " + e.getMessage(), e);
            return PacketBuilder.create(MessageType.LOGIN_RESPONSE)
                    .error("Server error: " + e.getMessage())
                    .build();
        }
    }

    public Packet handleRegister(Packet request) {
        try {
            Map<String, Object> data = request.getData();
            String username = (String) data.get("username");
            String password = (String) data.get("password");
            String email = (String) data.get("email");
            String fullName = (String) data.get("fullName");

            // Validate input
            if (username == null || username.trim().isEmpty()) {
                return PacketBuilder.create(MessageType.REGISTER_RESPONSE)
                        .error("Username is required")
                        .build();
            }

            if (password == null || password.length() < 6) {
                return PacketBuilder.create(MessageType.REGISTER_RESPONSE)
                        .error("Password must be at least 6 characters")
                        .build();
            }

            if (email == null || !isValidEmail(email)) {
                return PacketBuilder.create(MessageType.REGISTER_RESPONSE)
                        .error("Invalid email format")
                        .build();
            }

            // Kiểm tra username đã tồn tại
            if (userDAO.existsByUsername(username.trim())) {
                return PacketBuilder.create(MessageType.REGISTER_RESPONSE)
                        .error("Username already exists")
                        .build();
            }

            // Kiểm tra email đã tồn tại
            if (userDAO.existsByEmail(email.trim())) {
                return PacketBuilder.create(MessageType.REGISTER_RESPONSE)
                        .error("Email already registered")
                        .build();
            }

            // Tạo user mới
            User newUser = new User();
            newUser.setUsername(username.trim());
            newUser.setEmail(email.trim());
            newUser.setFullName(fullName != null ? fullName.trim() : username.trim());
            newUser.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
            newUser.setStatusType(User.UserStatus.OFFLINE);
            newUser.setActive(true);

            // Lưu vào database
            userDAO.insert(newUser);
            newUser.setPasswordHash(null);

            logger.info("User registered successfully: " + username);

            return PacketBuilder.create(MessageType.REGISTER_RESPONSE)
                    .success(true)
                    .put("message", "Registration successful")
                    .put("user", newUser)
                    .build();

        } catch (SQLException e) {
            logger.error("Database error during registration: " + e.getMessage(), e);
            return PacketBuilder.create(MessageType.REGISTER_RESPONSE)
                    .error("Database error: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            logger.error("Error during registration: " + e.getMessage(), e);
            return PacketBuilder.create(MessageType.REGISTER_RESPONSE)
                    .error("Server error: " + e.getMessage())
                    .build();
        }
    }

    public Packet handleLogout(Packet request) {
        try {
            Map<String, Object> data = request.getData();
            Long userId = data.get("userId") != null ? ((Number) data.get("userId")).longValue() : null;

            if (userId == null) {
                return PacketBuilder.create(MessageType.LOGOUT_RESPONSE)
                        .error("User ID is required")
                        .build();
            }

            userDAO.updateStatus(userId, User.UserStatus.OFFLINE, null, null);

            logger.info("User logged out: " + userId);

            return PacketBuilder.create(MessageType.LOGOUT_RESPONSE)
                    .success(true)
                    .put("message", "Logout successful")
                    .build();

        } catch (SQLException e) {
            logger.error("Database error during logout: " + e.getMessage(), e);
            return PacketBuilder.create(MessageType.LOGOUT_RESPONSE)
                    .error("Database error: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            logger.error("Error during logout: " + e.getMessage(), e);
            return PacketBuilder.create(MessageType.LOGOUT_RESPONSE)
                    .error("Server error: " + e.getMessage())
                    .build();
        }
    }



    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email != null && email.matches(emailRegex);
    }
}