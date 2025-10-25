package com.chatapp.client.controller.component;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ChatViewController {
    @FXML private Label contactNameLabel;
    @FXML private Label contactStatusLabel;
    @FXML private Label contactAvatarLabel;
    @FXML private ScrollPane messagesScrollPane;
    @FXML private VBox messagesContainer;
    @FXML private TextField messageInputField;

    @FXML
    public void initialize() {
        System.out.println("[CHAT VIEW] Initialized");
    }

    @FXML
    private void startVideoCall() {
        System.out.println("Start video call");
    }

    @FXML
    private void searchInChat() {
        System.out.println("Search in chat");
    }

    @FXML
    private void openChatMenu() {
        System.out.println("Open chat menu");
    }

    @FXML
    private void attachFile() {
        System.out.println("Attach file");
    }

    @FXML
    private void sendMessage() {
        String text = messageInputField.getText().trim();
        if (!text.isEmpty()) {
            System.out.println("Send message: " + text);
            messageInputField.clear();
        }
    }

    @FXML
    private void addEmoji() {
        System.out.println("Add emoji");
    }

    @FXML
    private void recordVoice() {
        System.out.println("Record voice");
    }
}