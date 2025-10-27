package com.chatapp.client.controller.component;

import com.chatapp.client.service.UserService;
import com.chatapp.common.protocol.Packet;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.util.Optional;

/**
 * Dialog for changing user password
 */
public class ChangePasswordDialog {

    private Long userId;

    public ChangePasswordDialog(Long userId) {
        this.userId = userId;
    }

    public void show() {
        // Create custom dialog
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Enter your current password and new password");

        // Set button types
        ButtonType changeButtonType = new ButtonType("Change", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changeButtonType, ButtonType.CANCEL);

        // Create password fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        PasswordField oldPassword = new PasswordField();
        oldPassword.setPromptText("Current Password");
        oldPassword.setPrefWidth(250);

        PasswordField newPassword = new PasswordField();
        newPassword.setPromptText("New Password");
        newPassword.setPrefWidth(250);

        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setPromptText("Confirm New Password");
        confirmPassword.setPrefWidth(250);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        errorLabel.setVisible(false);

        grid.add(new Label("Current Password:"), 0, 0);
        grid.add(oldPassword, 1, 0);
        grid.add(new Label("New Password:"), 0, 1);
        grid.add(newPassword, 1, 1);
        grid.add(new Label("Confirm Password:"), 0, 2);
        grid.add(confirmPassword, 1, 2);
        grid.add(errorLabel, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Request focus on old password field
        javafx.application.Platform.runLater(oldPassword::requestFocus);

        // Enable/disable change button based on input
        javafx.scene.Node changeButton = dialog.getDialogPane().lookupButton(changeButtonType);
        changeButton.setDisable(true);

        // Validation
        oldPassword.textProperty().addListener((observable, oldValue, newValue) -> {
            validateInput(oldPassword, newPassword, confirmPassword, errorLabel, changeButton);
        });

        newPassword.textProperty().addListener((observable, oldValue, newValue) -> {
            validateInput(oldPassword, newPassword, confirmPassword, errorLabel, changeButton);
        });

        confirmPassword.textProperty().addListener((observable, oldValue, newValue) -> {
            validateInput(oldPassword, newPassword, confirmPassword, errorLabel, changeButton);
        });

        // Convert result when change button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == changeButtonType) {
                return new Pair<>(oldPassword.getText(), newPassword.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(passwords -> {
            String oldPass = passwords.getKey();
            String newPass = passwords.getValue();

            // Send change password request to server in background thread
            new Thread(() -> {
                try {
                    UserService userService = UserService.getInstance();
                    Packet response = userService.changePassword(userId, oldPass, newPass);

                    javafx.application.Platform.runLater(() -> {
                        if (response.isSuccess()) {
                            showSuccessAlert();
                        } else {
                            String errorMsg = response.getError() != null ?
                                    response.getError() : "Failed to change password";
                            showErrorAlert(errorMsg);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> {
                        showErrorAlert("Connection error: " + e.getMessage());
                    });
                }
            }).start();
        });
    }

    private void validateInput(PasswordField oldPassword, PasswordField newPassword,
                               PasswordField confirmPassword, Label errorLabel,
                               javafx.scene.Node changeButton) {

        errorLabel.setVisible(false);

        String oldPass = oldPassword.getText();
        String newPass = newPassword.getText();
        String confirmPass = confirmPassword.getText();

        // Check if all fields are filled
        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            changeButton.setDisable(true);
            return;
        }

        // Check if new password is strong enough
        if (newPass.length() < 6) {
            errorLabel.setText("Password must be at least 6 characters");
            errorLabel.setVisible(true);
            changeButton.setDisable(true);
            return;
        }

        // Check if passwords match
        if (!newPass.equals(confirmPass)) {
            errorLabel.setText("Passwords do not match");
            errorLabel.setVisible(true);
            changeButton.setDisable(true);
            return;
        }

        // Check if new password is different from old
        if (oldPass.equals(newPass)) {
            errorLabel.setText("New password must be different");
            errorLabel.setVisible(true);
            changeButton.setDisable(true);
            return;
        }

        changeButton.setDisable(false);
    }

    private void showSuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Password changed successfully!");
        alert.showAndWait();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Password Change Failed");
        alert.setContentText(message);
        alert.showAndWait();
    }
}