package com.github.ondosh.chatbot.bot;

import com.github.ondosh.chatbot.model.UserProfile;

public class HybridBot implements IBot {

    private final GigaChatBot   gigaChatBot = new GigaChatBot();
    private final SimpleBot parser      = new SimpleBot();

    /**
     * Увеличивает счётчик сообщений пользователя на 1.
     * Делегирует вызов SimpleBot.
     */
    public void countUserMessage() {
        parser.countUserMessage();
    }

    /**
     * Увеличивает счётчик сообщений бота на 1.
     * Делегирует вызов SimpleBot.
     */
    public void countBotMessage() {
        parser.countBotMessage();
    }

    /**
     * Возвращает общее количество сообщений (пользователь + бот).
     * @return общее количество сообщений
     */
    public int getTotalMessages() {
        return parser.getTotalMessages();
    }

    /**
     * Возвращает количество сообщений, отправленных пользователем.
     * @return количество пользовательских сообщений
     */
    public int getUserMessages() {
        return parser.getUserMessages();
    }

    /**
     * Возвращает количество сообщений, отправленных ботом.
     * @return количество сообщений бота
     */
    public int getBotMessages() {
        return parser.getBotMessages();
    }

    /**
     * Устанавливает статистику сообщений вручную.
     * @param total общее количество сообщений
     * @param user количество сообщений пользователя
     * @param bot количество сообщений бота
     */
    public void setStats(int total, int user, int bot) {
        parser.setStats(total, user, bot);
    }

    /**
     * Получает ответ на основе входного сообщения.
     * Алгоритм обработки:
     * 1. Проверка на команды (время, дата, математика, статистика)
     * 2. Проверка на заготовленные фразы (приветствия, прощания)
     * 3. Отправка в нейросеть GigaChat, если ничего не совпало
     *
     * @param input входное сообщение от пользователя
     * @return ответ бота (команда, фраза или ответ нейросети)
     */
    @Override
    public String getResponse(String input) {
        // 1. Команды (время, дата, математика, статистика)
        if (parser.isCommand(input)) {
            return parser.executeCommand(input);
        }

        // 2. Заготовленные фразы (приветствия, прощания и т.д.)
        String phrase = parser.tryPhrase(input);
        if (phrase != null) {
            return phrase;
        }

        // 3. Нейросеть — только если ничего не совпало
        return gigaChatBot.getResponse(input);
    }

    /**
     * Устанавливает профиль пользователя для GigaChat бота.
     * @param profile профиль пользователя (имя, возраст, город)
     */
    public void setUserProfile(UserProfile profile) {
        gigaChatBot.setUserProfile(profile);
    }

    /**
     * Возвращает имя бота.
     * @return имя бота "HybridBot"
     */
    @Override
    public String getBotName() {
        return "HybridBot";
    }

    /**
     * Проверяет доступность GigaChat (нейросети).
     * @return true если GigaChat доступен, false в противном случае
     */
    @Override
    public boolean isAvailable() {
        return gigaChatBot.isAvailable();
    }
}