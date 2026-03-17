package com.github.ondosh.chatbot.controller;

import com.github.ondosh.chatbot.model.User;
import com.github.ondosh.chatbot.util.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class WelcomeController {

    @FXML
    private TextField nameField;

    @FXML
    private void onStartChat() {
        String name = nameField.getText().trim();

        // Не пускаем дальше с пустым именем
        if (name.isEmpty()) {
            nameField.setPromptText("Введите имя!");
            nameField.setStyle("-fx-border-color: red;");
            return;
        }

        User user = new User(name);

        Stage stage = (Stage) nameField.getScene().getWindow();

        // Переключаем сцену и сразу получаем контроллер второго окна
        ChatController chatController = SceneSwitcher.switchScene(
                stage,
                "/com/github/ondosh/chatbot/chat-view.fxml"
        );

        // Передаём пользователя в контроллер чата
        chatController.setUser(user);
    }
}