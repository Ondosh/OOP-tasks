package com.github.ondosh.chatbot.bot;

public interface IBot {
    // Основной метод ответа
    String getResponse(String input);

    // Проверка — является ли сообщение командой
    boolean isCommand(String input);

    // Выполнение команды с параметрами
    String executeCommand(String input);
}