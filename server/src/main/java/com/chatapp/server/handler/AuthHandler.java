package com.chatapp.server.handler;

import com.chatapp.common.model.User;
import com.chatapp.common.protocol.MessageType;
import com.chatapp.common.protocol.Packet;
import com.chatapp.common.protocol.PacketBuilder;
import com.chatapp.server.service.AuthService;
import com.chatapp.server.util.Logger;

public class AuthHandler {
    private final AuthService authService;
    private final Logger logger = Logger.getInstance();

    public AuthHandler() {
        this.authService = new AuthService();
    }

    /**
     * Handle login request
     */
    public Packet handleLogin(Packet request) {
        try {
            String username = request.getString("username");
            String password = request.getString("password");

            // Validate input
            if (username == null || password == null) {
                return PacketBuilder.create(MessageType.LOGIN_RESPONSE)
                        .success(false)
                        .error("Vui lòng nhập đầy đủ thông tin")
                        .build();
            }

            // Login
            User user = authService.login(username, password);

            // Return success response
            return PacketBuilder.create(MessageType.LOGIN_RESPONSE)
                    .success(true)
                    .put("userId", user.getId())
                    .put("username", user.getUsername())
                    .put("email", user.getEmail())
                    .put("fullName", user.getFullName())
                    .put("avatarUrl", user.getAvatarUrl())
                    .put("statusMessage", user.getStatusMessage())
                    .build();

        } catch (Exception e) {
            logger.error("Login failed: " + e.getMessage(), e);
            return PacketBuilder.create(MessageType.LOGIN_RESPONSE)
                    .success(false)
                    .error(e.getMessage())
                    .build();
        }
    }

    /**
     * Handle register request
     */
    public Packet handleRegister(Packet request) {
        try {
            String username = request.getString("username");
            String email = request.getString("email");
            String password = request.getString("password");
            String fullName = request.getString("fullName");

            // Validate input
            if (username == null || email == null || password == null) {
                return PacketBuilder.create(MessageType.REGISTER_RESPONSE)
                        .success(false)
                        .error("Vui lòng nhập đầy đủ thông tin")
                        .build();
            }

            // Register
            User user = authService.register(username, email, password, fullName);

            // Return success response
            return PacketBuilder.create(MessageType.REGISTER_RESPONSE)
                    .success(true)
                    .put("message", "Đăng ký thành công! Vui lòng đăng nhập.")
                    .build();

        } catch (Exception e) {
            logger.error("Registration failed: " + e.getMessage(), e);
            return PacketBuilder.create(MessageType.REGISTER_RESPONSE)
                    .success(false)
                    .error(e.getMessage())
                    .build();
        }
    }

    /**
     * Handle logout request
     */
    public Packet handleLogout(Packet request) {
        try {
            Long userId = request.getLong("userId");

            if (userId == null) {
                return PacketBuilder.create(MessageType.LOGOUT_RESPONSE)
                        .success(false)
                        .error("Invalid user ID")
                        .build();
            }

            // Logout
            authService.logout(userId);

            return PacketBuilder.create(MessageType.LOGOUT_RESPONSE)
                    .success(true)
                    .build();

        } catch (Exception e) {
            logger.error("Logout failed: " + e.getMessage(), e);
            return PacketBuilder.create(MessageType.LOGOUT_RESPONSE)
                    .success(false)
                    .error(e.getMessage())
                    .build();
        }
    }
}