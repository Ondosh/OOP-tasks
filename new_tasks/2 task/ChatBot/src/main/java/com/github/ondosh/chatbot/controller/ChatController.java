package com.github.ondosh.chatbot.controller;

import com.github.ondosh.chatbot.bot.HybridBot;
import com.github.ondosh.chatbot.bot.IBot;
import com.github.ondosh.chatbot.model.*;
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
    private IBot bot;

    // Состояние опроса профиля

    /** Мастер сбора профиля нового пользователя, null если профиль уже есть. */
    private ProfileWizard profileWizard;

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
            profileWizard = new ProfileWizard(user.getName());
            String firstQuestion = profileWizard.start();
            Message msg = new Message("Бот", firstQuestion, Message.Sender.BOT);
            messageList.getItems().add(msg);
            user.addMessage(msg);
            bot.countBotMessage();
        } else {
            // Загружаем статистику из файла
            int[] stats = StatsManager.loadStats();
            // Статистика сообщений
            bot.setStats(stats[0], stats[1], stats[2]);
            // Профиль пользователя с информацией которую он о себе дал
            bot.setUserProfile(profile);
        }

        // Загружаем историю сообщений из файла
        List<Message> history = HistoryManager.load(user.getName());
        if (!history.isEmpty()) {
            // Цикл загрузки  сообщений
            for (Message message : history) {
                // Добавляется сообщение в историю
                user.addMessage(message);
                // Добавляется в список сообщений
                messageList.getItems().add(message);
            }
            // Автоматический скролл к последнему сообщению
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

    @FXML
    private void onSendMessage() {
        // Очищаем от лишних пробелов и запоминаем сообщение
        String text = inputField.getText().trim();
        if (text.isBlank()) return; // если сообщение пустое ничего не делаем

        // Сообщение пользователя
        Message userMessage = new Message(user.getName(), text, Message.Sender.USER);

        // Добавляем в список, считаем сообщение, очищаем поле ввода для следующего
        messageList.getItems().add(userMessage);
        user.addMessage(userMessage);
        inputField.clear();
        bot.countUserMessage();

        // Если идёт опрос профиля — не отправляем боту сообщение
        // В onSendMessage вместо if (profileState != ProfileState.NONE):
        if (profileWizard != null && profileWizard.isActive()) {
            String reply = profileWizard.handle(text, p -> {
                ProfileManager.save(p);
                bot.setUserProfile(p);
            });
            Message msg = new Message("Бот", reply, Message.Sender.BOT);
            messageList.getItems().add(msg);
            user.addMessage(msg);
            bot.countBotMessage();
            messageList.scrollTo(messageList.getItems().size() - 1);
            return;
        }

        // Заглушка "Печатает..."
        Message typing = new Message("Бот", "Печатает...", Message.Sender.BOT);
        messageList.getItems().add(typing);

        // Объявляем отдельный поток, чтобы пока мы ждём ответа от бота, у нас не висла программа
        // Код передаётся как функциональный интерфейс типа Runnable. Метод void run()
        Thread thread = new Thread(() -> {
            // Ждём ответа от бота на сообщение пользователя
            String response = bot.getResponse(text);
            // После этого удаляем заглушку, добавляем ответ бота
            javafx.application.Platform.runLater(() -> {
                messageList.getItems().remove(typing);

                Message botMessage = new Message("Бот", response, Message.Sender.BOT);
                messageList.getItems().add(botMessage);
                user.addMessage(botMessage);

                messageList.scrollTo(messageList.getItems().size() - 1);
                // Считаем ответ бота После того, как он ответил
                bot.countBotMessage();
            });
        });
        // Daemon процесс это такой процесс, который убивается программой при закрытии контроллера
        // Например, бот ждёт ответа от сети, но пользователь закрыл окно
        // Тогда бот продолжит ждать ответ, а с setDaemon(true) этот процесс так же прервётся.
        thread.setDaemon(true);
        thread.start();

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
            // Выполняем метод родительского класса через super
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