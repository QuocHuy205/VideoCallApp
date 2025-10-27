package com.chatapp.client.controller.component;

import com.chatapp.client.controller.component.ProfileController;
import com.chatapp.client.service.AuthService;
import com.chatapp.common.model.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class SidebarMenuController {
    @FXML private VBox sidebarContainer;
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
        loadUserInfo();

        // Setup click handler for user profile card
        setupUserProfileClickHandler();

        // Test buttons
        if (chatsBtn != null) System.out.println("[SIDEBAR] Chats button found");
        if (settingsBtn != null) System.out.println("[SIDEBAR] Settings button found");
    }

    /**
     * Load và hiển thị thông tin user
     */
    private void loadUserInfo() {
        User currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            String username = currentUser.getUsername();
            userNameLabel.setText(username);

            if (username != null && !username.isEmpty()) {
                userAvatarLabel.setText(username.substring(0, 1).toUpperCase());
            }
        }
    }

    /**
     * Setup click handler cho user profile card để mở profile window
     */
    private void setupUserProfileClickHandler() {
        // Tìm Button có styleClass "user-profile-card" trong sidebar
        if (sidebarContainer != null) {
            for (javafx.scene.Node node : sidebarContainer.getChildren()) {
                if (node instanceof Button && node.getStyleClass().contains("user-profile-card")) {
                    ((Button) node).setOnAction(event -> openProfileWindow());
                    System.out.println("[SIDEBAR] User profile click handler setup");
                    break;
                }
            }
        }
    }

    /**
     * Mở cửa sổ Profile
     */
    private void openProfileWindow() {
        User currentUser = authService.getCurrentUser();

        if (currentUser == null) {
            showError("Lỗi", "Không tìm thấy thông tin người dùng!");
            System.err.println("[SIDEBAR] No user logged in!");
            return;
        }

        try {
            System.out.println("[SIDEBAR] Opening profile window for: " + currentUser.getUsername());

            // Load FXML
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/components/profile.fxml")
            );
            Parent root = loader.load();

            // Get controller và set user data
            ProfileController profileController = loader.getController();
            profileController.setUser(currentUser);

            // Tạo Stage mới (modal window)
            Stage profileStage = new Stage();
            profileStage.setTitle("Hồ sơ người dùng");
            profileStage.initModality(Modality.APPLICATION_MODAL);

            // Tạo scene
            Scene scene = new Scene(root, 500, 700);

            // Apply stylesheet
            try {
                scene.getStylesheets().add(
                        getClass().getResource("/css/main.css").toExternalForm()
                );
            } catch (Exception e) {
                System.err.println("[SIDEBAR] Warning: Could not load main.css for profile");
            }

            profileStage.setScene(scene);
            profileStage.setResizable(false);

            // Hiển thị và đợi
            profileStage.showAndWait();

            // Refresh user info sau khi đóng profile window
            loadUserInfo();
            System.out.println("[SIDEBAR] Profile window closed");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("[SIDEBAR] Error opening profile window: " + e.getMessage());
            showError("Lỗi", "Không thể mở cửa sổ hồ sơ: " + e.getMessage());
        }
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
                // Mở profile window
                openProfileWindow();
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

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}