package com.chatapp.server.network;

import com.chatapp.common.protocol.Packet;
import com.chatapp.common.protocol.PacketBuilder;
import com.chatapp.common.protocol.MessageType;
import com.chatapp.common.util.JsonUtil;
import com.chatapp.server.core.ClientRegistry;
import com.chatapp.server.service.FriendService;
import com.chatapp.server.service.UserService;
import com.chatapp.server.service.AuthService;
import com.chatapp.server.util.Logger;

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

    /**
     * Xử lý request và trả về response
     */
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
                    return authService.handleLogout(request);

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

                // Friend Management
                case ADD_FRIEND_REQUEST:
                    return FriendService.getInstance().handleSendFriendRequest(request);

                case ACCEPT_FRIEND_REQUEST:
                    return FriendService.getInstance().handleAcceptFriendRequest(request);

                case REJECT_FRIEND_REQUEST:
                    return FriendService.getInstance().handleRejectFriendRequest(request);

                case UNFRIEND_REQUEST:
                    return FriendService.getInstance().handleUnfriend(request);

                case BLOCK_FRIEND_REQUEST:
                    return FriendService.getInstance().handleBlockUser(request);

                case GET_FRIENDS_REQUEST:
                    return FriendService.getInstance().handleGetFriends(request);

                case GET_PENDING_REQUESTS_REQUEST:
                    return FriendService.getInstance().handleGetPendingRequests(request);

                case SEARCH_USERS_REQUEST:
                    return FriendService.getInstance().handleSearchUsers(request);

                // TODO: Thêm các handler khác (Chat, File, Call...)

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
            logger.info("Client disconnected: " + socket.getInetAddress());
        } catch (IOException e) {
            logger.error("Error during cleanup: " + e.getMessage());
        }
    }

    public Long getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(Long userId) {
        this.currentUserId = userId;
    }
}