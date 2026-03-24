package com.github.ondosh.chatbot.bot;

import com.github.ondosh.chatbot.model.UserProfile;

public class HybridBot implements IBot {

    private final GigaChatBot   gigaChatBot = new GigaChatBot();
    private final CommandParser parser      = new CommandParser();

    @Override
    public boolean isCommand(String input) {
        return parser.isCommand(input);
    }

    @Override
    public String executeCommand(String input) {
        return parser.executeCommand(input);
    }

    @Override
    public String getResponse(String input) {
        // 1. Команды (время, дата, математика, статистика)
        if (parser.isCommand(input)) {
            return parser.executeCommand(input);
        }

        // 2. Заготовленные фразы (приветствия, прощания и т.д.)
        if (parser.isPhrase(input)) {
            return parser.executePhrase(input);
        }

        // 3. Нейросеть — только если ничего не совпало
        return gigaChatBot.getResponse(input);
    }

    public CommandParser getParser() {
        return parser;
    }

    public void setUserProfile(UserProfile profile) {
        gigaChatBot.setUserProfile(profile);
    }
}