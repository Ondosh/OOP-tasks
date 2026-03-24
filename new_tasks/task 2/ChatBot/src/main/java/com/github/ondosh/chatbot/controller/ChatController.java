package com.github.ondosh.chatbot.controller;

import com.github.ondosh.chatbot.bot.IBot;
import com.github.ondosh.chatbot.bot.HybridBot;
import com.github.ondosh.chatbot.model.CurrentUser;
import com.github.ondosh.chatbot.model.Message;
import com.github.ondosh.chatbot.model.User;
import com.github.ondosh.chatbot.model.UserProfile;
import com.github.ondosh.chatbot.util.HistoryManager;

import com.github.ondosh.chatbot.util.ProfileManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.List;

public class ChatController {

    @FXML private TextField inputField;
    @FXML private ListView<Message> messageList;

    private User user;
    private final IBot bot = new HybridBot();

    private enum ProfileState { NONE, WAITING_AGE, WAITING_CITY }
    private ProfileState profileState = ProfileState.NONE;
    private String pendingAge = null;

    @FXML
    public void initialize() {
        messageList.setCellFactory(list -> new MessageCell());
    }

    public void setUser(User user) {
        this.user = user;
        CurrentUser.set(user);

        // Загружаем профиль и передаём в бот
        UserProfile profile = ProfileManager.load(user.getName());
        if (profile == null) {
            askForProfile(user.getName());
        } else {
            ((HybridBot) bot).setUserProfile(profile);
        }

        // Загружаем историю
        List<Message> history = HistoryManager.load(user.getName());
        if (!history.isEmpty()) {
            for (Message message : history) {
                user.addMessage(message);
                messageList.getItems().add(message);
            }
            messageList.scrollTo(messageList.getItems().size() - 1);
        }
    }

    private void askForProfile(String name) {
        profileState = ProfileState.WAITING_AGE;
        Message msg = new Message("Бот",
                "Привет, " + name + "! Давай познакомимся. Сколько тебе лет?",
                Message.Sender.BOT);
        messageList.getItems().add(msg);
        user.addMessage(msg);
    }

    @FXML
    private void onSendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        Message userMessage = new Message(user.getName(), text, Message.Sender.USER);
        messageList.getItems().add(userMessage);
        user.addMessage(userMessage);
        inputField.clear();

        // Перехватываем ввод если идёт опрос профиля
        if (profileState != ProfileState.NONE) {
            handleProfileInput(text);
            return;
        }

        // Обычная обработка сообщения
        ((HybridBot) bot).getParser().countUserMessage();
        Message typing = new Message("Бот", "Печатает...", Message.Sender.BOT);
        messageList.getItems().add(typing);
        getThread(text, typing).start();
    }

    private Thread getThread(String text, Message typing) {
        Thread thread = new Thread(() -> {
            String response = bot.getResponse(text);

            javafx.application.Platform.runLater(() -> {
                messageList.getItems().remove(typing);

                Message botMessage = new Message("Бот", response, Message.Sender.BOT);
                messageList.getItems().add(botMessage);

                // Сохраняем ответ бота в историю
                user.addMessage(botMessage);
                ((HybridBot) bot).getParser().countBotMessage();

                messageList.scrollTo(messageList.getItems().size() - 1);
            });
        });
        thread.setDaemon(true);
        return thread;
    }

    // Кастомная ячейка с переносом текста
    private static class MessageCell extends ListCell<Message> {

        private final Label label = new Label();
        private final HBox container = new HBox(label);

        public MessageCell() {
            // Включаем перенос текста
            label.setWrapText(true);

            // Ширина label привязана к ширине ячейки с отступами
            label.maxWidthProperty().bind(
                    listViewProperty()
                            .flatMap(lv -> lv.widthProperty())
                            .map(w -> w.doubleValue() - 40)
            );

            label.setStyle("-fx-padding: 6 10 6 10; -fx-background-radius: 8;");
        }

        @Override
        protected void updateItem(Message message, boolean empty) {
            super.updateItem(message, empty);

            if (empty || message == null) {
                setGraphic(null);
                return;
            }

            label.setText(message.toString());

            // Разные стили для пользователя и бота
            if (message.getSender() == Message.Sender.USER) {
                label.setStyle(
                        "-fx-background-color: #DCF8C6;" +
                                "-fx-padding: 6 10 6 10;" +
                                "-fx-background-radius: 8;" +
                                "-fx-text-fill: #000000;"
                );
                container.setStyle("-fx-alignment: CENTER-RIGHT;");
            } else {
                label.setStyle(
                        "-fx-background-color: #FFFFFF;" +
                                "-fx-padding: 6 10 6 10;" +
                                "-fx-background-radius: 8;" +
                                "-fx-text-fill: #000000;" +
                                "-fx-border-color: #E0E0E0;" +
                                "-fx-border-radius: 8;"
                );
                container.setStyle("-fx-alignment: CENTER-LEFT;");
            }

            container.maxWidthProperty().bind(
                    listViewProperty()
                            .flatMap(lv -> lv.widthProperty())
                            .map(w -> w.doubleValue() - 20)
            );

            setGraphic(container);
            setStyle("-fx-background-color: transparent;");
        }
    }

    private void handleProfileInput(String text) {
        if (profileState == ProfileState.WAITING_AGE) {
            pendingAge = text;
            profileState = ProfileState.WAITING_CITY;

            Message msg = new Message("Бот", "Из какого ты города?", Message.Sender.BOT);
            messageList.getItems().add(msg);
            user.addMessage(msg);

        } else if (profileState == ProfileState.WAITING_CITY) {
            int age = 0;
            try { age = Integer.parseInt(pendingAge.trim()); } catch (NumberFormatException ignored) {}

            UserProfile profile = new UserProfile(user.getName(), age, text);
            ProfileManager.save(profile);
            ((HybridBot) bot).setUserProfile(profile);

            profileState = ProfileState.NONE;
            pendingAge = null;

            Message msg = new Message("Бот",
                    "Запомнил! " + age + " лет, город — " + text + ".",
                    Message.Sender.BOT);
            messageList.getItems().add(msg);
            user.addMessage(msg);
            messageList.scrollTo(messageList.getItems().size() - 1);
        }
    }
}