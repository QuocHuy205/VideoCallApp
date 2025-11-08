package com.chatapp.client.controller;

import com.chatapp.client.network.ServerConnection;
import com.chatapp.client.service.AuthService;
import com.chatapp.common.model.User;
import com.chatapp.common.protocol.Packet;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    private AuthService authService;
    private ServerConnection connection;

    @FXML
    public void initialize() {
        authService = AuthService.getInstance();
        connection = ServerConnection.getInstance();
        connectToServer();
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                connection.connect("localhost", 8888);
                Platform.runLater(() -> loginButton.setDisable(false));
            } catch (IOException e) {
                Platform.runLater(() -> {
                    showError("Không thể kết nối server!");
                    loginButton.setDisable(true);
                });
            }
        }).start();
        loginButton.setDisable(true);
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập đầy đủ thông tin");
            return;
        }

        loginButton.setDisable(true);
        loginButton.setText("Đang đăng nhập...");

        new Thread(() -> {
            try {
                Packet response = authService.login(username, password);
                Platform.runLater(() -> {
                    if (response.isSuccess()) {

                        // Sau khi response.isSuccess()
                        if (response.isSuccess()) {
                            // ✅ Parse user data
                            Object userData = response.get("user");
                            if (userData instanceof Map) {
                                Map<String, Object> userMap = (Map<String, Object>) userData;

                                User user = new User();
                                user.setId(((Number) userMap.get("id")).longValue());
                                user.setUsername((String) userMap.get("username"));
                                user.setEmail((String) userMap.get("email"));
                                user.setFullName((String) userMap.get("fullName"));
                                user.setStatusType(User.UserStatus.valueOf(userMap.get("statusType").toString()));

                                // ✅ CRITICAL: Set current user
                                authService.setCurrentUser(user);
                                System.out.println("[LOGIN] User set: " + user.getUsername());
                            }

                            showMainWindow();
                        }
                        showMainWindow();
                    } else {
                        showError(response.getError());
                        loginButton.setDisable(false);
                        loginButton.setText("LOGIN");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Đăng nhập thất bại: " + e.getMessage());
                    loginButton.setDisable(false);
                    loginButton.setText("LOGIN");
                });
            }
        }).start();
    }

    @FXML
    private void handleRegister() {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/register.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1000, 650);
            scene.getStylesheets().add(getClass().getResource("/css/auth.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("ChatApp - Đăng ký");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleForgotPassword() {
        showInfo("Chức năng đang phát triển");
    }

    private void showMainWindow() {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1200, 800);
            // Load main.css for main window
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("ChatApp - " + authService.getCurrentUser().getUsername());
            stage.setResizable(true);
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}