package com.github.ondosh.chatbot.controller;

import com.github.ondosh.chatbot.model.User;
import com.github.ondosh.chatbot.util.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Контроллер экрана приветствия.
 * Отвечает за ввод имени пользователя и переход к основному окну чата.
 */
public class WelcomeController {

    /** Поле ввода имени пользователя, привязано к FXML. */
    @FXML
    private TextField nameField;

    /**
     * Обрабатывает нажатие кнопки «Начать чат».
     * Валидирует имя, создаёт пользователя и переключает сцену на чат.
     */
    @FXML
    private void onStartChat() {
        String name = nameField.getText().trim();

        // Не пускаем дальше с пустым именем — подсвечиваем поле красным
        if (name.isEmpty()) {
            nameField.setPromptText("Введите имя!");
            nameField.setStyle("-fx-border-color: red;");
            return;
        }

        User user = new User(name);

        // Получаем текущее окно для передачи в SceneSwitcher
        Stage stage = (Stage) nameField.getScene().getWindow();

        // Переключаем сцену и получаем контроллер нового окна
        ChatController chatController = SceneSwitcher.switchScene(
                stage,
                "/com/github/ondosh/chatbot/chat-view.fxml"
        );

        // Передаём пользователя — контроллер загрузит профиль и историю
        chatController.setUser(user);
    }
}