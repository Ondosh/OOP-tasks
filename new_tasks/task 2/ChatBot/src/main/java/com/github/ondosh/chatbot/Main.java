package com.github.ondosh.chatbot;

import com.github.ondosh.chatbot.model.CurrentUser;
import com.github.ondosh.chatbot.util.HistoryManager;
import com.github.ondosh.chatbot.util.ProfileManager;
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
        HistoryManager.save(CurrentUser.get());
        ProfileManager.save(CurrentUser.getProfile());
    }

    public static void main(String[] args) {
        launch(args);
    }
}