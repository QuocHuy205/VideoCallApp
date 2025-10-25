package com.chatapp.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load login view
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
        Parent root = loader.load();

        // Create scene with exact size
        Scene scene = new Scene(root, 1000, 650);
        scene.getStylesheets().add(getClass().getResource("/css/light-theme.css").toExternalForm());

        // Configure stage
        primaryStage.setTitle("ChatApp - Đăng nhập");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // Không cho resize
        primaryStage.centerOnScreen(); // Canh giữa màn hình
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}