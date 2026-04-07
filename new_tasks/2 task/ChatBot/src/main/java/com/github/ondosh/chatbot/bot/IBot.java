package com.github.ondosh.chatbot.bot;

import com.github.ondosh.chatbot.model.UserProfile;

public interface IBot {

    /** Возвращает ответ на сообщение пользователя. */
    String getResponse(String input);

    /** Возвращает имя/идентификатор бота. */
    default String getBotName() {
        return "Bot";
    }

    /** Проверяет доступность бота. */
    default boolean isAvailable() {
        return true;
    }

    /** Учитывает сообщение пользователя в статистике. */
    default void countUserMessage() {}

    /** Учитывает ответ бота в статистике. */
    default void countBotMessage() {}

    /** Устанавливает профиль пользователя для персонализации. */
    default void setUserProfile(UserProfile profile) {}
}