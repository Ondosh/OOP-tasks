package com.github.ondosh.chatbot.bot;

public class HybridBot implements IBot {

    private final SimpleChatBot localBot   = new SimpleChatBot();
    private final GigaChatBot gigaChatBot = new GigaChatBot();
    private final CommandParser  parser    = new CommandParser();

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
        // Сначала проверяем команды
        if (isCommand(input)) {
            return executeCommand(input);
        }

        // Затем словарь
        if (localBot.hasMatch(input)) {
            return localBot.getResponse(input);
        }

        // Иначе — LLM
        return gigaChatBot.getResponse(input);
    }

    public CommandParser getParser() {
        return parser;
    }
}