package com.github.ondosh.chatbot.bot;

public interface IBot {

    /** Возвращает ответ на сообщение пользователя. */
    String getResponse(String input);

    /** Возвращает имя/идентификатор бота (например, "GigaChat", "HybridBot"). */
    default String getBotName() {
        return "Bot";
    }

    /** Проверяет доступность бота (например, есть ли соединение с API). */
    default boolean isAvailable() {
        return true;
    }
}