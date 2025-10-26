package com.chatapp.client.controller;

import com.chatapp.client.service.AuthService;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class MainController {

    @FXML private Button globalToggleBtn;
    @FXML private VBox sidebarContainer;

    private boolean isSidebarVisible = false; // MẶC ĐỊNH ẨN

    @FXML
    public void initialize() {
        System.out.println("[MAIN] Main window initialized");

        // Get current user
        AuthService authService = AuthService.getInstance();
        if (authService.getCurrentUser() != null) {
            System.out.println("[MAIN] User: " + authService.getCurrentUser().getUsername());
        }

        // ẨN SIDEBAR KHI VỪA VÀO
        if (sidebarContainer != null) {
            sidebarContainer.setVisible(false);
            sidebarContainer.setManaged(false);
            sidebarContainer.setTranslateX(-260);
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