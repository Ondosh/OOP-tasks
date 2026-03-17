package com.github.ondosh.chatbot;

import com.github.ondosh.chatbot.model.CurrentUser;
import com.github.ondosh.chatbot.util.HistoryManager;
import javafx.application.Application;
import javafx.stage.Stage;
import com.github.ondosh.chatbot.util.SceneSwitcher;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        SceneSwitcher.switchScene(stage,
                "/com/github/ondosh/chatbot/welcome-view.fxml"
        );
        stage.setTitle("ChatBot");
        stage.show();
    }

    @Override
    public void stop() {
        // Вызывается автоматически при закрытии окна
        HistoryManager.save(CurrentUser.get());
    }

    public static void main(String[] args) {
        launch(args);
    }
}