package com.chatapp.server.network;

import com.chatapp.common.protocol.Packet;
import com.chatapp.common.protocol.PacketBuilder;
import com.chatapp.common.protocol.MessageType;
import com.chatapp.common.util.JsonUtil;
import com.chatapp.server.service.UserService;
import com.chatapp.server.service.AuthService;
import com.chatapp.server.util.Logger;
import com.chatapp.server.core.ClientRegistry;

import java.io.*;
import java.net.Socket;

/**
 * Handler xử lý từng client connection
 */
public class ClientHandler implements Runnable {
    private final Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private final Logger logger = Logger.getInstance();
    private Long currentUserId;
    private final UserService userService;
    private final AuthService authService;

    public ClientHandler(Socket socket, ClientRegistry clientRegistry) {
        this.socket = socket;
        this.userService = UserService.getInstance();
        this.authService = AuthService.getInstance();
    }

    @Override
    public void run() {
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            logger.info("Client connected: " + socket.getInetAddress());

            String line;
            while ((line = input.readLine()) != null) {
                logger.info("Received: " + line);

                try {
                    Packet request = JsonUtil.fromJson(line, Packet.class);
                    Packet response = handleRequest(request);

                    // Nếu login thành công, lưu vào ClientRegistry
                    if (request.getType() == MessageType.LOGIN_REQUEST && response.isSuccess()) {
                        Long userId = response.getData().get("user") != null
                                ? ((com.chatapp.common.model.User) response.getData().get("user")).getId()
                                : null;
                        this.currentUserId = userId;
                        if (userId != null) {
                            ClientRegistry.getInstance().addClient(userId, this);
                        }
                    }

                    String responseJson = JsonUtil.toJson(response);
                    output.println(responseJson);
                    output.flush();

                    logger.info("Sent: " + responseJson);
                } catch (Exception e) {
                    logger.error("Error processing request: " + e.getMessage(), e);
                    Packet errorResponse = PacketBuilder.create(MessageType.ERROR)
                            .error("Server error: " + e.getMessage())
                            .build();
                    output.println(JsonUtil.toJson(errorResponse));
                }
            }

        } catch (IOException e) {
            logger.error("Connection error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private Packet handleRequest(Packet request) {
        MessageType type = request.getType();

        try {
            switch (type) {
                // Authentication
                case LOGIN_REQUEST:
                    return authService.handleLogin(request);
                case REGISTER_REQUEST:
                    return authService.handleRegister(request);
                case LOGOUT_REQUEST:
                    Packet logoutResp = authService.handleLogout(request);
                    // Remove client khỏi registry khi logout
                    if (currentUserId != null) {
                        ClientRegistry.getInstance().removeClient(currentUserId);
                    }
                    return logoutResp;

                // Profile Management
                case UPDATE_PROFILE_REQUEST:
                    return userService.handleUpdateProfile(request);
                case CHANGE_PASSWORD_REQUEST:
                    return userService.handleChangePassword(request);
                case UPLOAD_AVATAR_REQUEST:
                    return userService.handleUploadAvatar(request);
                case GET_USER_INFO_REQUEST:
                    return userService.handleGetUserInfo(request);
                case STATUS_UPDATE:
                    return userService.handleStatusUpdate(request);

                default:
                    return PacketBuilder.create(MessageType.ERROR)
                            .error("Unsupported message type: " + type)
                            .build();
            }
        } catch (Exception e) {
            logger.error("Error handling request: " + e.getMessage(), e);
            return PacketBuilder.create(MessageType.ERROR)
                    .error("Server error: " + e.getMessage())
                    .build();
        }
    }

    private void cleanup() {
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null) socket.close();
            if (currentUserId != null) {
                ClientRegistry.getInstance().removeClient(currentUserId);
            }
            logger.info("Client disconnected: " + socket.getInetAddress());
        } catch (IOException e) {
            logger.error("Error during cleanup: " + e.getMessage());
        }
    }

    public Long getCurrentUserId() {
        return currentUserId;
    }
}
