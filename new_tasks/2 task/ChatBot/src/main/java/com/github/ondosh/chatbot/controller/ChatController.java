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
     * Экземпляр бота. Используется интерфейс IBot для слабой связанности,
     * но в некоторых местах приводится к HybridBot для доступа к специфичным методам
     * (getParser, setUserProfile).
     */
    private final IBot bot = new HybridBot();

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
        messageList.setCellFactory(list -> new MessageCell());
    }

    /**
     * Устанавливает текущего пользователя и подготавливает чат к работе.
     * Вызывается из WelcomeController после ввода имени.
     *
     * Логика:
     * 1. Сохраняем пользователя глобально (для Main.stop()).
     * 2. Пробуем загрузить профиль — если не найден, запускаем опрос прямо в чате.
     * 3. Загружаем историю сообщений из файла.
     *
     * @param user объект пользователя с именем
     */
    public void setUser(User user) {
        this.user = user;

        // Сохраняем глобально — нужно для сохранения при закрытии в Main.stop()
        CurrentUser.set(user);

        // Пробуем загрузить профиль пользователя
        UserProfile profile = ProfileManager.load(user.getName());
        if (profile == null) {
            // Первый запуск — профиля нет, запускаем диалог прямо в чате
            askForProfile(user.getName());
        } else {
            // Профиль найден — передаём в бот для персонализации системного промпта
            ((HybridBot) bot).setUserProfile(profile);
        }

        // Загружаем историю сообщений из файла
        List<Message> history = HistoryManager.load(user.getName());
        if (!history.isEmpty()) {
            for (Message message : history) {
                user.addMessage(message);
                messageList.getItems().add(message);
            }
            // Прокручиваем список к последнему сообщению
            messageList.scrollTo(messageList.getItems().size() - 1);
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
    }

    // Обработка отправки сообщения

    /**
     * Обрабатывает нажатие кнопки «Отправить» (или Enter).
     *
     * Если идёт опрос профиля — перехватывает ввод и передаёт в {@link #handleProfileInput}.
     * Иначе — добавляет сообщение в чат и запускает запрос к боту в отдельном потоке.
     */
    @FXML
    private void onSendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        // Добавляем сообщение пользователя в чат и историю
        Message userMessage = new Message(user.getName(), text, Message.Sender.USER);
        messageList.getItems().add(userMessage);
        user.addMessage(userMessage);
        inputField.clear();

        // Если идёт опрос профиля — перехватываем ввод, не отправляем боту
        if (profileState != ProfileState.NONE) {
            handleProfileInput(text);
            return;
        }

        // Учитываем сообщение пользователя в статистике
        ((HybridBot) bot).getParser().countUserMessage();

        // Показываем индикатор «Печатает...» пока бот думает
        Message typing = new Message("Бот", "Печатает...", Message.Sender.BOT);
        messageList.getItems().add(typing);

        // Запрос к боту выполняется в daemon-потоке, чтобы не блокировать UI
        getThread(text, typing).start();
    }

    /**
     * Создаёт поток для асинхронного получения ответа от бота.
     * После получения ответа обновляет UI через Platform.runLater(),
     * так как JavaFX не допускает изменение UI из не-главного потока.
     *
     * @param text   текст сообщения пользователя
     * @param typing сообщение-заглушка «Печатает...», которое нужно удалить после ответа
     * @return настроенный daemon-поток
     */
    private Thread getThread(String text, Message typing) {
        Thread thread = new Thread(() -> {
            // Получаем ответ от бота (может занять время при обращении к GigaChat)
            String response = bot.getResponse(text);

            // Все изменения UI должны выполняться в потоке JavaFX
            javafx.application.Platform.runLater(() -> {
                // Убираем заглушку «Печатает...»
                messageList.getItems().remove(typing);

                // Добавляем реальный ответ бота
                Message botMessage = new Message("Бот", response, Message.Sender.BOT);
                messageList.getItems().add(botMessage);
                user.addMessage(botMessage);

                // Учитываем ответ бота в статистике
                ((HybridBot) bot).getParser().countBotMessage();

                messageList.scrollTo(messageList.getItems().size() - 1);
            });
        });

        // Daemon-поток завершается вместе с приложением, не блокируя выход
        thread.setDaemon(true);
        return thread;
    }

    // Опрос профиля пользователя в чате

    /**
     * Обрабатывает ввод пользователя во время опроса профиля.
     * Работает как простой конечный автомат с двумя состояниями:
     * WAITING_AGE → WAITING_CITY → NONE (опрос завершён).
     *
     * @param text текст, введённый пользователем
     */
    private void handleProfileInput(String text) {
        if (profileState == ProfileState.WAITING_AGE) {
            // Сохраняем возраст временно и переходим к следующему вопросу
            pendingAge = text;
            profileState = ProfileState.WAITING_CITY;

            Message msg = new Message("Бот", "Из какого ты города?", Message.Sender.BOT);
            messageList.getItems().add(msg);
            user.addMessage(msg);

        } else if (profileState == ProfileState.WAITING_CITY) {
            // Парсим возраст — если пользователь ввёл не число, используем 0
            int age = 0;
            try {
                age = Integer.parseInt(pendingAge.trim());
            } catch (NumberFormatException ignored) {
            }

            // Создаём профиль, сохраняем в файл и передаём боту
            UserProfile profile = new UserProfile(user.getName(), age, text);
            ProfileManager.save(profile);
            ((HybridBot) bot).setUserProfile(profile);

            // Сбрасываем состояние опроса
            profileState = ProfileState.NONE;
            pendingAge = null;

            // Подтверждаем сохранение пользователю
            Message msg = new Message("Бот",
                    "Запомнил! " + age + " лет, город — " + text + ".",
                    Message.Sender.BOT);
            messageList.getItems().add(msg);
            user.addMessage(msg);
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
                            .flatMap(lv -> lv.widthProperty())
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
                    listViewProperty()
                            .flatMap(lv -> lv.widthProperty())
                            .map(w -> w.doubleValue() - 20)
            );

            setGraphic(container);

            // Убираем стандартный фон ячейки ListView, чтобы не перекрывал пузыри
            setStyle("-fx-background-color: transparent;");
        }
    }
}