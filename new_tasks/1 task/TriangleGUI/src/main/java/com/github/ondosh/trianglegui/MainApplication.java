package com.github.ondosh.trianglegui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Главный класс приложения — калькулятор треугольника.
 * Наследует {@link Application} и служит точкой входа для JavaFX.
 */
public class MainApplication extends Application {

    /**
     * Инициализирует и отображает главное окно приложения.
     *
     * @param stage главная сцена, предоставляемая JavaFX-рантаймом
     * @throws IOException если файл разметки main-window.fxml не найден или повреждён
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-window.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 350, 400);
        stage.setTitle("Калькулятор треугольника");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Точка входа в программу. Запускает JavaFX-приложение.
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        launch();
    }
}