package com.github.ondosh.chatbot.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Утилитный класс для переключения сцен в JavaFX.
 * Инкапсулирует шаблонный код загрузки FXML, чтобы не дублировать его в контроллерах.
 */
public class SceneSwitcher {

    /**
     * Загружает FXML-файл, устанавливает новую сцену на Stage и возвращает контроллер.
     *
     * Универсальный тип T позволяет получить контроллер нужного типа без явного приведения:
     * {@code ChatController controller = SceneSwitcher.switchScene(stage, "chat-view.fxml");}
     *
     * @param stage    текущее окно приложения, в котором меняется сцена
     * @param fxmlPath путь к FXML-файлу относительно classpath
     * @param <T>      тип контроллера загружаемой сцены
     * @return контроллер загруженной сцены
     * @throws RuntimeException если файл не найден или содержит ошибки —
     * оборачиваем IOException в RuntimeException,
     * так как ошибка загрузки сцены некритична и требует исправления кода,
     * а не обработки в рантайме
     */
    public static <T> T switchScene(Stage stage, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneSwitcher.class.getResource(fxmlPath)
            );
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.show();

            // Возвращаем контроллер — вызывающий код может сразу передать в него данные
            return loader.getController();
        } catch (IOException e) {
            // IOException при загрузке FXML означает либо неверный путь, либо ошибку в разметке
            throw new RuntimeException("Не удалось загрузить сцену: " + fxmlPath, e);
        }
    }
}