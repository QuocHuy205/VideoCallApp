package com.chatapp.client.controller.component;

import com.chatapp.client.service.AuthService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.Optional;

public class SidebarMenuController {
    @FXML private Label userNameLabel;
    @FXML private Label userAvatarLabel;

    @FXML private Button chatsBtn;
    @FXML private Button contactsBtn;
    @FXML private Button callsBtn;
    @FXML private Button savedBtn;
    @FXML private Button settingsBtn;

    private Button activeButton;
    private AuthService authService;

    @FXML
    public void initialize() {
        System.out.println("[SIDEBAR] Initialized");

        activeButton = chatsBtn;
        authService = AuthService.getInstance();

        // Load user info
        if (authService.getCurrentUser() != null) {
            String username = authService.getCurrentUser().getUsername();
            userNameLabel.setText(username);

            if (username != null && !username.isEmpty()) {
                userAvatarLabel.setText(username.substring(0, 1).toUpperCase());
            }
        }

        // Test buttons
        if (chatsBtn != null) System.out.println("[SIDEBAR] Chats button found");
        if (settingsBtn != null) System.out.println("[SIDEBAR] Settings button found");
    }

    @FXML
    private void showChats() {
        System.out.println("[SIDEBAR] ✅ Chats clicked!");
        setActiveButton(chatsBtn);
    }

    @FXML
    private void showContacts() {
        System.out.println("[SIDEBAR] ✅ Contacts clicked!");
        setActiveButton(contactsBtn);
        showInfo("Danh bạ", "Đang phát triển...");
    }

    @FXML
    private void showCalls() {
        System.out.println("[SIDEBAR] ✅ Calls clicked!");
        setActiveButton(callsBtn);
        showInfo("Cuộc gọi", "Đang phát triển...");
    }

    @FXML
    private void showSavedMessages() {
        System.out.println("[SIDEBAR] ✅ Saved messages clicked!");
        setActiveButton(savedBtn);
        showInfo("Tin nhắn đã lưu", "Đang phát triển...");
    }

    @FXML
    private void openSettings() {
        System.out.println("[SIDEBAR] ✅ Settings clicked!");
        setActiveButton(settingsBtn);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cài đặt");
        alert.setHeaderText("Tùy chọn");
        alert.setContentText("Chọn hành động:");

        ButtonType btnProfile = new ButtonType("Xem Profile");
        ButtonType btnLogout = new ButtonType("Đăng xuất");
        ButtonType btnCancel = new ButtonType("Hủy");

        alert.getButtonTypes().setAll(btnProfile, btnLogout, btnCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == btnProfile) {
                showInfo("Profile", "Đang phát triển...");
            } else if (result.get() == btnLogout) {
                handleLogout();
            }
        }
    }

    private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Đăng xuất");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn đăng xuất?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            new Thread(() -> {
                try {
                    authService.logout();
                    System.out.println("[SIDEBAR] ✅ Logged out");

                    Platform.runLater(() -> {
                        try {
                            Stage stage = (Stage) settingsBtn.getScene().getWindow();
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
                            Parent root = loader.load();

                            Scene scene = new Scene(root, 1000, 650);
                            scene.getStylesheets().clear();
                            scene.getStylesheets().add(getClass().getResource("/css/auth.css").toExternalForm());

                            stage.setScene(scene);
                            stage.setTitle("ChatApp - Đăng nhập");
                            stage.setResizable(false);
                            stage.centerOnScreen();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("menu-item-active");
        }
        if (button != null) {
            button.getStyleClass().add("menu-item-active");
            activeButton = button;
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}