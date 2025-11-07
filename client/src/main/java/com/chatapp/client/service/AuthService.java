package com.chatapp.client.service;

import com.chatapp.common.model.User;
import com.chatapp.common.protocol.MessageType;
import com.chatapp.common.protocol.Packet;
import com.chatapp.common.protocol.PacketBuilder;
import com.chatapp.client.network.ServerConnection;
import com.chatapp.client.util.PreferenceManager;

public class AuthService {
    private static AuthService instance;
    private ServerConnection connection;
    private User currentUser;

    private AuthService() {
        this.connection = ServerConnection.getInstance();
    }

    // comment
    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    public Packet login(String username, String password) throws Exception {
        System.out.println("[AUTH] Login attempt: " + username);

        Packet request = PacketBuilder.create(MessageType.LOGIN_REQUEST)
                .put("username", username)
                .put("password", password)
                .build();

        Packet response = connection.sendAndReceive(request);

        if (response.isSuccess()) {
            currentUser = new User();
            currentUser.setId(response.getLong("userId"));
            currentUser.setUsername(response.getString("username"));
            currentUser.setEmail(response.getString("email"));
            currentUser.setFullName(response.getString("fullName"));

            PreferenceManager.getInstance().setCurrentUser(currentUser);
            System.out.println("[AUTH] Login successful: " + currentUser.getUsername());
        } else {
            System.out.println("[AUTH] Login failed: " + response.getError());
        }

        return response;
    }

    public Packet register(String username, String email, String password, String fullName) throws Exception {
        System.out.println("[AUTH] Registration attempt: " + username);

        Packet request = PacketBuilder.create(MessageType.REGISTER_REQUEST)
                .put("username", username)
                .put("email", email)
                .put("password", password)
                .put("fullName", fullName)
                .build();

        Packet response = connection.sendAndReceive(request);

        if (response.isSuccess()) {
            System.out.println("[AUTH] Registration successful");
        } else {
            System.out.println("[AUTH] Registration failed: " + response.getError());
        }

        return response;
    }

    public void logout() {
        if (currentUser != null) {
            System.out.println("[AUTH] Logout: " + currentUser.getUsername());
            currentUser = null;
            PreferenceManager.getInstance().clearCurrentUser();
        }
        connection.disconnect();
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}