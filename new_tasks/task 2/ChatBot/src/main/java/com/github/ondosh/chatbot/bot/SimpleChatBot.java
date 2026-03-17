package com.github.ondosh.chatbot.bot;

import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleChatBot implements IBot {

    // LinkedHashMap чтобы проверка шла в порядке добавления
    private static final Map<String, String> PHRASES = new LinkedHashMap<>();

    static {
        // Приветствия
        PHRASES.put("привет",       "Привет! Как дела?");
        PHRASES.put("здравствуй",   "Здравствуй! Рад тебя видеть.");
        PHRASES.put("хай",          "Хай! Чем могу помочь?");
        PHRASES.put("добрый день",  "Добрый день! Чем могу помочь?");

        // Прощания
        PHRASES.put("пока",         "Пока! Было приятно пообщаться.");
        PHRASES.put("до свидания",  "До свидания! Заходи ещё.");
        PHRASES.put("увидимся",     "Увидимся! Удачи тебе.");

        // Вопросы о боте
        PHRASES.put("как тебя зовут", "Меня зовут ChatBot. А тебя?");
        PHRASES.put("кто ты?",         "Я простой чат-бот, написанный на Java.");
        PHRASES.put("кто здесь?",      "кто.");
        PHRASES.put("что ты умеешь?",  "Я умею отвечать на простые вопросы и поддерживать беседу.");

        // Состояние
        PHRASES.put("как дела?",     "Всё отлично, спасибо что спросил!");
        PHRASES.put("как ты?",       "Работаю в штатном режиме, спасибо!");
        PHRASES.put("что делаешь?",  "Жду твоих сообщений!");

        // Благодарности
        PHRASES.put("спасибо",      "Пожалуйста! Обращайся.");
        PHRASES.put("благодарю",    "Всегда рад помочь!");

        // Прочее
        PHRASES.put("помощь",       "Просто напиши мне что-нибудь, и я отвечу!");
        PHRASES.put("помоги",       "Конечно помогу! Что тебя интересует?");
    }

    @Override
    public String getResponse(String input) {
        String lower = input.toLowerCase().trim();

        // Ищем вхождение ключевой фразы в сообщении пользователя
        for (Map.Entry<String, String> entry : PHRASES.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return getFallback(lower);
    }

    public boolean hasMatch(String input) {
        String lower = input.toLowerCase().trim();
        for (String key : PHRASES.keySet()) {
            if (lower.contains(key)) {
                return true;
            }
        }
        return false;
    }
    private String getFallback(String input) {
        if (input.endsWith("?")) {
            return "Хороший вопрос! Я пока не знаю ответа на это.";
        }
        if (input.length() < 4) {
            return "Можешь написать подробнее?";
        }
        return "Интересно... Расскажи мне больше об этом.";
    }

    @Override
    public boolean isCommand(String input) {
        return false; // SimpleChatBot не обрабатывает команды
    }

    @Override
    public String executeCommand(String input) {
        return ""; // SimpleChatBot не выполняет команды
    }
}