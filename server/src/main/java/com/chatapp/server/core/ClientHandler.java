package com.chatapp.server.core;

import com.chatapp.common.protocol.MessageType;
import com.chatapp.common.protocol.Packet;
import com.chatapp.common.util.JsonUtil;
import com.chatapp.server.handler.AuthHandler;
import com.chatapp.server.util.Logger;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ClientRegistry clientRegistry;
    private final Logger logger = Logger.getInstance();
    private BufferedReader input;
    private PrintWriter output;
    private Long userId;

    private final AuthHandler authHandler;

    public ClientHandler(Socket socket, ClientRegistry clientRegistry) {
        this.socket = socket;
        this.clientRegistry = clientRegistry;
        this.authHandler = new AuthHandler();
    }

    @Override
    public void run() {
        try {
            socket.setTcpNoDelay(true);

            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            logger.info("Client connected: " + socket.getInetAddress());

            String line;
            while ((line = input.readLine()) != null) {
                logger.info("[SERVER] <<< RECV: " + line);

                try {
                    Packet request = JsonUtil.fromJson(line, Packet.class);
                    Packet response = handlePacket(request);

                    if (response != null) {
                        String responseJson = JsonUtil.toJson(response);
                        logger.info("[SERVER] >>> SEND: " + responseJson);

                        output.println(responseJson);
                        output.flush();
                    }

                } catch (Exception e) {
                    logger.error("Error processing request: " + e.getMessage(), e);

                    Packet errorResponse = new Packet(MessageType.ERROR);
                    errorResponse.setError("Server error: " + e.getMessage());

                    String errorJson = JsonUtil.toJson(errorResponse);
                    output.println(errorJson);
                    output.flush();
                }
            }

        } catch (IOException e) {
            logger.error("Client handler error: " + e.getMessage(), e);
        } finally {
            cleanup();
        }
    }

    private Packet handlePacket(Packet request) {
        MessageType type = request.getType();
        logger.info("[SERVER] Handling: " + type);

        try {
            switch (type) {
                case LOGIN_REQUEST:
                    Packet loginResponse = authHandler.handleLogin(request);
                    if (loginResponse.isSuccess()) {
                        userId = loginResponse.getLong("userId");
                        clientRegistry.addClient(userId, this);
                    }
                    return loginResponse;

                case REGISTER_REQUEST:
                    return authHandler.handleRegister(request);

                case LOGOUT_REQUEST:
                    if (userId != null) {
                        clientRegistry.removeClient(userId);
                    }
                    return authHandler.handleLogout(request);

                case VERIFY_OTP_REQUEST:
                    return authHandler.handleVerifyOTP(request);

                case RESEND_OTP_REQUEST:
                    return authHandler.handleResendOTP(request);

                case FORGOT_PASSWORD_REQUEST:
                    return authHandler.handleForgotPassword(request);

                case RESET_PASSWORD_REQUEST:
                    return authHandler.handleResetPassword(request);

                default:
                    Packet errorResponse = new Packet(MessageType.ERROR);
                    errorResponse.setError("Unknown message type: " + type);
                    return errorResponse;
            }
        } catch (Exception e) {
            logger.error("Handler exception: " + e.getMessage(), e);
            Packet errorResponse = new Packet(MessageType.ERROR);
            errorResponse.setError("Handler error: " + e.getMessage());
            return errorResponse;
        }
    }

    private void cleanup() {
        try {
            if (userId != null) {
                clientRegistry.removeClient(userId);
            }
            if (socket != null) socket.close();
            if (input != null) input.close();
            if (output != null) output.close();
            logger.info("Client disconnected");
        } catch (Exception e) {
            logger.error("Cleanup error: " + e.getMessage(), e);
        }
    }
}