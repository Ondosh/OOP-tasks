package com.github.ondosh.chatbot;

import com.github.ondosh.chatbot.model.CurrentUser;
import com.github.ondosh.chatbot.util.HistoryManager;
import com.github.ondosh.chatbot.util.ProfileManager;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import com.github.ondosh.chatbot.util.SceneSwitcher;

import java.util.Objects;

/**
 * Точка входа в приложение.
 * Наследует {@link Application} — это требование JavaFX для запуска UI.
 */
public class Main extends Application {

    /**
     * Вызывается JavaFX при старте приложения.
     * Загружает экран приветствия и устанавливает заголовок окна.
     *
     * @param stage главное окно приложения, создаётся JavaFX автоматически
     */
    @Override
    public void start(Stage stage) {
        SceneSwitcher.switchScene(stage,
                "/com/github/ondosh/chatbot/welcome-view.fxml"
        );
        stage.setTitle("ChatBot");
        stage.show();
        stage.getIcons().add(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icon.png")))
        );
    }

    /**
     * Вызывается JavaFX автоматически при закрытии окна.
     * Сохраняет историю сообщений и профиль пользователя в файлы,
     * чтобы данные не потерялись между сессиями.
     */
    @Override
    public void stop() {
        HistoryManager.save(CurrentUser.get());
        ProfileManager.save(CurrentUser.getProfile());
    }

    /**
     * Стандартная точка входа Java.
     * Делегирует запуск в JavaFX через {@link Application#launch}.
     */
    public static void main(String[] args) {
        launch(args);
    }
}