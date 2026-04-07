package com.github.ondosh.chatbot.bot;

import com.github.ondosh.chatbot.model.UserProfile;

public class HybridBot implements IBot {

    private final GigaChatBot   gigaChatBot = new GigaChatBot();
    private final SimpleBot parser      = new SimpleBot();

    public void countUserMessage() {
        parser.countUserMessage();
    }

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

    public SimpleBot getParser() {
        return parser;
    }

    public void setUserProfile(UserProfile profile) {
        gigaChatBot.setUserProfile(profile);
    }

    @Override
    public String getBotName() {
        return "HybridBot";
    }

    @Override
    public boolean isAvailable() {
        return gigaChatBot.isAvailable();
    }
}