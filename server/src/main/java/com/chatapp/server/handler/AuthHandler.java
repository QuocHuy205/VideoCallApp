package com.chatapp.server.handler;

import com.chatapp.common.protocol.MessageType;
import com.chatapp.common.protocol.Packet;
import com.chatapp.common.protocol.PacketBuilder;
import com.chatapp.server.service.AuthService;
import com.chatapp.server.util.Logger;

public class AuthHandler {
    private final AuthService authService;
    private final Logger logger = Logger.getInstance();

    public AuthHandler() {
        this.authService = AuthService.getInstance();
    }

    public Packet handleLogin(Packet request) {
        try {
            if (request == null) {
                logger.error("Login request is null");
                return PacketBuilder.create(MessageType.LOGIN_RESPONSE)
                        .error("Invalid login request")
                        .build();
            }

            Packet response = authService.handleLogin(request);

            if (response == null) {
                logger.error("AuthService.handleLogin returned null");
                return PacketBuilder.create(MessageType.LOGIN_RESPONSE)
                        .error("Internal server error (null response)")
                        .build();
            }

            return response;

        } catch (Exception e) {
            e.printStackTrace(); // in stack trace lên console
            logger.error("Login failed: " + e.getMessage(), e);
            return PacketBuilder.create(MessageType.LOGIN_RESPONSE)
                    .error(e.getMessage() != null ? e.getMessage() : "Internal server error")
                    .build();
        }

    }

    public Packet handleRegister(Packet request) {
        try {
            return authService.handleRegister(request);
        } catch (Exception e) {
            logger.error("Error handling register: " + e.getMessage(), e);
            return PacketBuilder.create(MessageType.REGISTER_RESPONSE)
                    .error("Server error: " + e.getMessage())
                    .build();
        }
    }

    public Packet handleLogout(Packet request) {
        try {
            return authService.handleLogout(request);
        } catch (Exception e) {
            logger.error("Error handling logout: " + e.getMessage(), e);
            return PacketBuilder.create(MessageType.LOGOUT_RESPONSE)
                    .error("Server error: " + e.getMessage())
                    .build();
        }
    }
}
