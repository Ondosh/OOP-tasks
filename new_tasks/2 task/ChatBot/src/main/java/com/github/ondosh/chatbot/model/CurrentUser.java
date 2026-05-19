package com.github.ondosh.chatbot.model;

/**
 * Глобальный контейнер для доступа к текущему пользователю и его профилю.
 * Используется в Main.stop() для сохранения данных при закрытии приложения.
 */
public class CurrentUser {

    /** Текущий пользователь сессии. */
    private static User user;

    /** Профиль текущего пользователя (возраст, город). */
    private static UserProfile profile;

    /**
     * Ставит переданного пользователя как текущего пользователя сессии
     * @param u
     */
    public static void set(User u)               { user = u; }

    /**
     * Геттер User
     * @return текущий пользователь сессии
     */
    public static User get()                     { return user; }

    /**
     * Возвращает профиль текущего пользователя
     * @return Профиль текущего пользователя
     */
    public static UserProfile getProfile()       { return profile; }
}