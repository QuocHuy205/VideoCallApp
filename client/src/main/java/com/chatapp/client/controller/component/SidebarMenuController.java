package com.chatapp.client.controller.component;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class SidebarMenuController {
    @FXML private Label userFullNameLabel;
    @FXML private Label userPhoneLabel;
    @FXML private Label userAvatarLabel;

    @FXML
    public void initialize() {
        // Will be populated from main controller
    }

    @FXML
    private void toggleMenu() {
        System.out.println("Toggle menu");
    }

    @FXML
    private void showChats() {
        System.out.println("Show chats");
    }

    @FXML
    private void showSavedMessages() {
        System.out.println("Show saved messages");
    }

    @FXML
    private void showContacts() {
        System.out.println("Show contacts");
    }

    @FXML
    private void showCalls() {
        System.out.println("Show calls");
    }

    @FXML
    private void showArchived() {
        System.out.println("Show archived");
    }

    @FXML
    private void showFeatures() {
        System.out.println("Show features");
    }

    @FXML
    private void reportBug() {
        System.out.println("Report bug");
    }

    @FXML
    private void openSettings() {
        System.out.println("Open settings");
    }
}