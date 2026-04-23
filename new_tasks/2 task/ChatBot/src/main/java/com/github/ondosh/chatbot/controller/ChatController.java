package com.github.ondosh.chatbot.controller;

import com.github.ondosh.chatbot.bot.HybridBot;
import com.github.ondosh.chatbot.model.CurrentUser;
import com.github.ondosh.chatbot.model.Message;
import com.github.ondosh.chatbot.model.User;
import com.github.ondosh.chatbot.model.UserProfile;
import com.github.ondosh.chatbot.util.HistoryManager;
import com.github.ondosh.chatbot.util.ProfileManager;
import com.github.ondosh.chatbot.util.StatsManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.util.List;

import static com.github.ondosh.chatbot.util.StatsManager.clearStats;

/**
 * Контроллер основного окна чата.
 * Связывает UI (FXML-разметку) с логикой бота и моделью данных.
 * Отвечает за:
 * — отображение и отправку сообщений;
 * — загрузку/сохранение истории и профиля пользователя;
 * — первичный опрос нового пользователя прямо в чате.
 */
public class ChatController {

    /** Поле ввода сообщения, привязано к FXML. */
    @FXML private TextField inputField;

    /** Список сообщений чата, привязан к FXML. */
    @FXML private ListView<Message> messageList;

    /** Текущий пользователь, устанавливается через {@link #setUser(User)}. */
    private User user;

    /**
     * Экземпляр бота.
     */
    private HybridBot bot;

    // Состояние опроса профиля

    /**
     * Перечисление состояний диалога при сборе профиля нового пользователя.
     * NONE — обычный режим чата;
     * WAITING_AGE — бот ждёт ввода возраста;
     * WAITING_CITY — бот ждёт ввода города.
     */
    private enum ProfileState { NONE, WAITING_AGE, WAITING_CITY }

    /** Текущее состояние опроса. По умолчанию — обычный режим. */
    private ProfileState profileState = ProfileState.NONE;

    /** Временно хранит введённый возраст между двумя шагами опроса. */
    private String pendingAge = null;

    // Инициализация

    /**
     * Вызывается JavaFX автоматически после загрузки FXML.
     * Устанавливает фабрику ячеек для кастомного отображения сообщений.
     */
    @FXML
    public void initialize() {
        // Устанавливаем фабрику ячеек
        messageList.setCellFactory(lv -> new MessageCell());

        // Добавляем обработчик закрытия окна
        messageList.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obs2, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        newWindow.setOnCloseRequest(event -> saveAll());
                    }
                });
            }
        });
    }

    /**
     * Устанавливает текущего пользователя и подготавливает чат к работе.
     */
    public void setUser(User user) {
        this.user = user;
        this.bot = new HybridBot(); // Создаём бота здесь, когда user уже известен

        // Сохраняем глобально
        CurrentUser.set(user);

        // Пробуем загрузить профиль пользователя
        UserProfile profile = ProfileManager.load(user.getName());
        if (profile == null) {
            clearStats();
            askForProfile(user.getName());
        } else {
            // Загружаем статистику из файла
            int[] stats = StatsManager.loadStats();
            bot.setStats(stats[0], stats[1], stats[2]);
            bot.setUserProfile(profile);
        }

        // Загружаем историю сообщений из файла
        List<Message> history = HistoryManager.load(user.getName());
        if (!history.isEmpty()) {
            for (Message message : history) {
                user.addMessage(message);
                messageList.getItems().add(message);
            }
            messageList.scrollTo(messageList.getItems().size() - 1);
        }
    }

    /**
     * Сохраняет историю и статистику при закрытии окна.
     */
    public void saveAll() {
        if (user != null && bot != null) {
            HistoryManager.save(user);
            StatsManager.saveStats(
                    user.getName(),
                    bot.getTotalMessages(),
                    bot.getUserMessages(),
                    bot.getBotMessages()
            );
        }
    }

    /**
     * Запускает диалог сбора профиля нового пользователя.
     * Переключает состояние в WAITING_AGE и отправляет первый вопрос в чат.
     *
     * @param name имя пользователя для персонализации приветствия
     */
    private void askForProfile(String name) {
        profileState = ProfileState.WAITING_AGE;
        Message msg = new Message("Бот",
                "Привет, " + name + "! Давай познакомимся. Сколько тебе лет?",
                Message.Sender.BOT);
        messageList.getItems().add(msg);
        user.addMessage(msg);
        bot.countBotMessage(); // Считаем сообщение бота
    }

    @FXML
    private void onSendMessage() {
        String text = inputField.getText().trim();
        if (text.isBlank()) return;

        // Сообщение пользователя
        Message userMessage = new Message(user.getName(), text, Message.Sender.USER);
        messageList.getItems().add(userMessage);
        user.addMessage(userMessage);
        inputField.clear();
        bot.countUserMessage();

        // Если идёт опрос профиля — не отправляем боту
        if (profileState != ProfileState.NONE) {
            handleProfileInput(text);
            return;
        }

        // Заглушка "Печатает..."
        Message typing = new Message("Бот", "Печатает...", Message.Sender.BOT);
        messageList.getItems().add(typing);

        // Объявляем отдельный поток, чтобы пока мы ждём ответа от бота, у нас не висла программа
        new Thread(() -> {
            String response = bot.getResponse(text);

            javafx.application.Platform.runLater(() -> {
                messageList.getItems().remove(typing);

                Message botMessage = new Message("Бот", response, Message.Sender.BOT);
                messageList.getItems().add(botMessage);
                user.addMessage(botMessage);

                messageList.scrollTo(messageList.getItems().size() - 1);
            });
        }).start();

        bot.countBotMessage();
    }

    /**
     * Обрабатывает ввод пользователя во время опроса профиля.
     * Работает как простой конечный автомат с двумя состояниями:
     * WAITING_AGE → WAITING_CITY → NONE (опрос завершён).
     *
     * @param text текст, введённый пользователем
     */
    private void handleProfileInput(String text) {
        if (profileState == ProfileState.WAITING_AGE) {
            pendingAge = text;
            profileState = ProfileState.WAITING_CITY;

            Message msg = new Message("Бот", "Из какого ты города?", Message.Sender.BOT);
            messageList.getItems().add(msg);
            user.addMessage(msg);
            bot.countBotMessage();

        } else if (profileState == ProfileState.WAITING_CITY) {
            int age = 0;
            try {
                age = Integer.parseInt(pendingAge.trim());
            } catch (NumberFormatException ignored) {
            }

            UserProfile profile = new UserProfile(user.getName(), age, text);
            ProfileManager.save(profile);
            bot.setUserProfile(profile);

            profileState = ProfileState.NONE;
            pendingAge = null;

            Message msg = new Message("Бот",
                    "Запомнил! " + age + " лет, город — " + text + ".",
                    Message.Sender.BOT);
            messageList.getItems().add(msg);
            user.addMessage(msg);
            bot.countBotMessage();

            messageList.scrollTo(messageList.getItems().size() - 1);
        }
    }

    // Кастомная ячейка списка сообщений

    /**
     * Кастомная ячейка для ListView, отображающая сообщение с переносом текста
     * и разным оформлением для пользователя и бота (по аналогии с мессенджерами).
     */
    private static class MessageCell extends ListCell<Message> {

        /** Текстовая метка с содержимым сообщения. */
        private final Label label = new Label();

        /** Контейнер для выравнивания пузыря влево или вправо. */
        private final HBox container = new HBox(label);

        public MessageCell() {
            // Разрешаем перенос длинного текста на новую строку
            label.setWrapText(true);

            // Ширина label привязана к ширине ячейки минус отступы,
            // чтобы текст не выходил за пределы при изменении размера окна
            label.maxWidthProperty().bind(
                    listViewProperty()
                            .flatMap(Region::widthProperty)
                            .map(w -> w.doubleValue() - 40)
            );

            label.setStyle("-fx-padding: 6 10 6 10; -fx-background-radius: 8;");
        }

        /**
         * Вызывается JavaFX при отрисовке или обновлении ячейки.
         * Применяет разные стили в зависимости от отправителя:
         * — пользователь: зелёный пузырь справа;
         * — бот: белый пузырь с рамкой слева.
         *
         * @param message объект сообщения (может быть null для пустых ячеек)
         * @param empty   true, если ячейка не содержит данных
         */
        @Override
        protected void updateItem(Message message, boolean empty) {
            super.updateItem(message, empty);

            // Пустые ячейки не отображаем
            if (empty || message == null) {
                setGraphic(null);
                return;
            }

            label.setText(message.toString());

            if (message.getSender() == Message.Sender.USER) {
                // Сообщения пользователя — зелёный фон, выравнивание вправо
                label.setStyle(
                        "-fx-background-color: #DCF8C6;" +
                                "-fx-padding: 6 10 6 10;" +
                                "-fx-background-radius: 8;" +
                                "-fx-text-fill: #000000;"
                );
                container.setStyle("-fx-alignment: CENTER-RIGHT;");
            } else {
                // Сообщения бота — белый фон с серой рамкой, выравнивание влево
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

            // Ширина контейнера тоже привязана к ширине списка
            container.maxWidthProperty().bind(
                    listViewProperty()                              // Получаем свойство, содержащее ссылку на ListView
                    .flatMap(Region::widthProperty)                 // Когда ListView появляется, берём его свойство ширины
                    .map(w -> w.doubleValue() - 20)         // Вычитаем 20 пикселей (отступы) из ширины ListView
            );

            setGraphic(container); // включаем кастомное содержимое, описанное выше

            // Убираем стандартный фон ячейки ListView, чтобы не перекрывал пузыри
            setStyle("-fx-background-color: transparent;");
        }
    }
}