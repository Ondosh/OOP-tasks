package com.github.ondosh.chatbot.controller;

import com.github.ondosh.chatbot.bot.IBot;
import com.github.ondosh.chatbot.bot.HybridBot;
import com.github.ondosh.chatbot.model.CurrentUser;
import com.github.ondosh.chatbot.model.Message;
import com.github.ondosh.chatbot.model.User;
import com.github.ondosh.chatbot.util.HistoryManager;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.util.List;

public class ChatController {

    @FXML private TextField inputField;
    @FXML private ListView<Message> messageList;

    private User user;
    private final IBot bot = new HybridBot();

    @FXML
    public void initialize() {
        messageList.setCellFactory(list -> new MessageCell());
    }

    public void setUser(User user) {
        this.user = user;

        // Сохраняем пользователя глобально для Main.stop()
        CurrentUser.set(user);

        // Загружаем историю из файла
        List<Message> history = HistoryManager.load(user.getName());

        if (!history.isEmpty()) {
            for (Message message : history) {
                user.addMessage(message);
                messageList.getItems().add(message);
            }
            messageList.scrollTo(messageList.getItems().size() - 1);
        } else {
            // Первый запуск — показываем приветствие
            Message greeting = new Message(
                    "Бот", "Привет, " + user.getName() + "!", Message.Sender.BOT);
            messageList.getItems().add(greeting);
            user.addMessage(greeting);
        }
    }

    @FXML
    private void onSendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        Message userMessage = new Message(user.getName(), text, Message.Sender.USER);
        messageList.getItems().add(userMessage);

        // Сохраняем в историю
        user.addMessage(userMessage);
        ((HybridBot) bot).getParser().countUserMessage();

        inputField.clear();

        Message typing = new Message("Бот", "Печатает...", Message.Sender.BOT);
        messageList.getItems().add(typing);

        Thread thread = getThread(text, typing);
        thread.start();
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
}