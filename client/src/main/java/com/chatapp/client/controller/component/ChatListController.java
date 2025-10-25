package com.chatapp.client.controller.component;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class ChatListController {
    @FXML private TextField searchField;
    @FXML private ListView<?> chatListView;
    @FXML private Button allChatsTab;

    @FXML
    public void initialize() {
        System.out.println("[CHAT LIST] Initialized");
    }

    @FXML
    private void startNewChat() {
        System.out.println("Start new chat");
    }

    @FXML
    private void showAllChats() {
        System.out.println("Show all chats");
    }

    @FXML
    private void showProjects() {
        System.out.println("Show projects");
    }

    @FXML
    private void showImportant() {
        System.out.println("Show important");
    }
}