package com.chatapp.client.controller;

import com.chatapp.client.controller.component.SidebarMenuController;
import com.chatapp.client.service.AuthService;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class MainController {

    @FXML private Button globalToggleBtn;
    @FXML private VBox sidebarContainer;
    @FXML private HBox chatListContainer;     // Container chứa chat-list HOẶC friend-list
    @FXML private HBox chatViewContainer;     // Container chứa chat-view HOẶC contact-detail

    private boolean isSidebarVisible = false;
    private SidebarMenuController sidebarController;

    @FXML
    public void initialize() {
        System.out.println("[MAIN] Main window initialized");

        // Get current user
        AuthService authService = AuthService.getInstance();
        if (authService.getCurrentUser() != null) {
            System.out.println("[MAIN] User: " + authService.getCurrentUser().getUsername());
        }

        // Load sidebar và set containers
        loadSidebar();

        // Ẩn sidebar khi vừa vào
        if (sidebarContainer != null) {
            sidebarContainer.setVisible(false);
            sidebarContainer.setManaged(false);
            sidebarContainer.setTranslateX(-260);
        }

        // Load default views (Chat List + Chat View)
        loadDefaultViews();
    }

    /**
     * Load sidebar và connect với containers
     */
    private void loadSidebar() {
        try {
            FXMLLoader sidebarLoader = new FXMLLoader(
                    getClass().getResource("/view/components/sidebar-menu.fxml")
            );
            Parent sidebar = sidebarLoader.load();
            sidebarController = sidebarLoader.getController();

            // QUAN TRỌNG: Set containers để sidebar có thể switch views
            if (sidebarController != null) {
                sidebarController.setContainers(chatListContainer, chatViewContainer);
                System.out.println("[MAIN] Sidebar controller connected with containers");
            }

            // Clear existing content và add sidebar
            sidebarContainer.getChildren().clear();
            sidebarContainer.getChildren().add(sidebar);

            System.out.println("[MAIN] Sidebar loaded successfully");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[MAIN] Failed to load sidebar: " + e.getMessage());
        }
    }

    /**
     * Load default views (Chat)
     */
    private void loadDefaultViews() {
        try {
            // Load Chat List
            FXMLLoader chatListLoader = new FXMLLoader(
                    getClass().getResource("/view/components/chat-list.fxml")
            );
            Parent chatList = chatListLoader.load();
            chatListContainer.getChildren().add(chatList);

            // Load Chat View
            FXMLLoader chatViewLoader = new FXMLLoader(
                    getClass().getResource("/view/components/chat-view.fxml")
            );
            Parent chatView = chatViewLoader.load();
            chatViewContainer.getChildren().add(chatView);

            System.out.println("[MAIN] Default views (Chat) loaded");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[MAIN] Failed to load default views: " + e.getMessage());
        }
    }

    @FXML
    private void toggleSidebar() {
        System.out.println("[MAIN] Toggle sidebar clicked");

        if (sidebarContainer == null) {
            System.err.println("[MAIN] Error: sidebarContainer is null!");
            return;
        }

        if (isSidebarVisible) {
            hideSidebar();
        } else {
            showSidebar();
        }
    }

    private void hideSidebar() {
        System.out.println("[MAIN] Hiding sidebar");

        TranslateTransition transition = new TranslateTransition(Duration.millis(250), sidebarContainer);
        transition.setToX(-260);
        transition.setOnFinished(event -> {
            sidebarContainer.setVisible(false);
            sidebarContainer.setManaged(false);
        });
        transition.play();
        isSidebarVisible = false;
    }

    private void showSidebar() {
        System.out.println("[MAIN] Showing sidebar");

        sidebarContainer.setVisible(true);
        sidebarContainer.setManaged(true);

        TranslateTransition transition = new TranslateTransition(Duration.millis(250), sidebarContainer);
        transition.setFromX(-260);
        transition.setToX(0);
        transition.play();
        isSidebarVisible = true;
    }
}