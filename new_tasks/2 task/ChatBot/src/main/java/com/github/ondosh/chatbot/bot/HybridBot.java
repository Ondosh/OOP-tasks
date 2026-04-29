package com.github.ondosh.chatbot.bot;

import com.github.ondosh.chatbot.model.UserProfile;

public class HybridBot implements IBot {

    private final GigaChatBot   gigaChatBot = new GigaChatBot();
    private final SimpleBot parser      = new SimpleBot();


    public void countUserMessage() {
        parser.countUserMessage();
    }

    public void countBotMessage() {
        parser.countBotMessage();
    }

    public int getTotalMessages() {
        return parser.getTotalMessages();
    }

    public int getUserMessages() {
        return parser.getUserMessages();
    }

    public int getBotMessages() {
        return parser.getBotMessages();
    }

    public void setStats(int total, int user, int bot) {
        parser.setStats(total, user, bot);
    }

    /**
     * Проверяем какой именно инпут нам прилетел.
     * 1. Проверка на команды из SimpleBot.
     * 2. Проверка на заготовленные фразы из SimpleBot.
     * 3. Отправляем в нейросеть если не совпало.
     * @param input
     * @return
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

    public void setUserProfile(UserProfile profile) {
        gigaChatBot.setUserProfile(profile);
    }

    @Override
    public String getBotName() {
        return "HybridBot";
    }

    /**
     * Проверка доступен ли GigaChat
     * @return boolean
     */
    @Override
    public boolean isAvailable() {
        return gigaChatBot.isAvailable();
    }
}