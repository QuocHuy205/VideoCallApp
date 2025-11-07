package com.chatapp.client.controller.component;

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
import javafx.scene.layout.HBox;
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

    // Containers từ MainController
    private HBox chatListContainer;
    private HBox chatViewContainer;

    @FXML
    public void initialize() {
        System.out.println("[SIDEBAR] Initialized");

        activeButton = chatsBtn;
        authService = AuthService.getInstance();

        // Load user info
        loadUserInfo();

        // Setup click handler for user profile card
        setupUserProfileClickHandler();
    }

    /**
     * Set containers để có thể switch giữa Chat và Contact view
     */
    public void setContainers(HBox chatListContainer, HBox chatViewContainer) {
        this.chatListContainer = chatListContainer;
        this.chatViewContainer = chatViewContainer;
        System.out.println("[SIDEBAR] Containers set");
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
            return;
        }

        try {
            System.out.println("[SIDEBAR] Opening profile window for: " + currentUser.getUsername());

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/components/profile.fxml")
            );
            Parent root = loader.load();

            ProfileController profileController = loader.getController();
            profileController.setUser(currentUser);

            Stage profileStage = new Stage();
            profileStage.setTitle("Hồ sơ người dùng");
            profileStage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(root, 500, 700);
            try {
                scene.getStylesheets().add(
                        getClass().getResource("/css/main.css").toExternalForm()
                );
            } catch (Exception e) {
                System.err.println("[SIDEBAR] Warning: Could not load main.css for profile");
            }

            profileStage.setScene(scene);
            profileStage.setResizable(false);
            profileStage.showAndWait();

            loadUserInfo();
            System.out.println("[SIDEBAR] Profile window closed");

        } catch (IOException e) {
            e.printStackTrace();
            showError("Lỗi", "Không thể mở cửa sổ hồ sơ: " + e.getMessage());
        }
    }

    /**
     * Show Chats View
     */
    @FXML
    private void showChats() {
        System.out.println("[SIDEBAR] ✅ Chats clicked!");
        setActiveButton(chatsBtn);

        if (chatListContainer == null || chatViewContainer == null) {
            System.err.println("[SIDEBAR] Containers not set!");
            return;
        }

        try {
            // Load Chat List
            FXMLLoader chatListLoader = new FXMLLoader(
                    getClass().getResource("/view/components/chat-list.fxml")
            );
            Parent chatList = chatListLoader.load();

            // Load Chat View
            FXMLLoader chatViewLoader = new FXMLLoader(
                    getClass().getResource("/view/components/chat-view.fxml")
            );
            Parent chatView = chatViewLoader.load();

            // Replace content
            chatListContainer.getChildren().clear();
            chatListContainer.getChildren().add(chatList);

            chatViewContainer.getChildren().clear();
            chatViewContainer.getChildren().add(chatView);

            System.out.println("[SIDEBAR] Switched to Chats");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[SIDEBAR] Failed to load Chats: " + e.getMessage());
            showError("Lỗi", "Không thể tải giao diện Chat");
        }
    }

    /**
     * Show Contacts View
     */
    @FXML
    private void showContacts() {
        System.out.println("[SIDEBAR] ✅ Contacts clicked!");
        setActiveButton(contactsBtn);

        if (chatListContainer == null || chatViewContainer == null) {
            System.err.println("[SIDEBAR] Containers not set!");
            showError("Lỗi", "Containers chưa được khởi tạo!");
            return;
        }

        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                showError("Lỗi", "Vui lòng đăng nhập lại!");
                return;
            }

            // Load Contact List (friend-list.fxml)
            FXMLLoader contactListLoader = new FXMLLoader(
                    getClass().getResource("/view/components/friend-list.fxml")
            );
            Parent contactList = contactListLoader.load();

            // Get controller and set current user
            FriendListController contactListController = contactListLoader.getController();
            contactListController.setCurrentUser(currentUser);

            // Load Contact Detail View
            FXMLLoader contactDetailLoader = new FXMLLoader(
                    getClass().getResource("/view/components/contact-detail.fxml")
            );
            Parent contactDetail = contactDetailLoader.load();

            // Get controller and set current user
            ContactDetailController contactDetailController = contactDetailLoader.getController();
            contactDetailController.setCurrentUser(currentUser);

            // Replace content
            chatListContainer.getChildren().clear();
            chatListContainer.getChildren().add(contactList);

            chatViewContainer.getChildren().clear();
            chatViewContainer.getChildren().add(contactDetail);

            System.out.println("[SIDEBAR] Switched to Contacts");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[SIDEBAR] Failed to load Contacts: " + e.getMessage());
            showError("Lỗi", "Không thể tải giao diện Contacts: " + e.getMessage());
        }
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