package com.github.ondosh.chatbot.model;

import java.util.function.Consumer;

/**
 * Конечный автомат для сбора профиля нового пользователя.
 * Инкапсулирует состояния опроса, переходы между ними,
 * парсинг и валидацию введённых данных.
 */
public class ProfileWizard {

    public enum State { NONE, WAITING_AGE, WAITING_CITY }

    private State state = State.NONE;
    private String pendingAge = null;
    private final String userName;

    public ProfileWizard(String userName) {
        this.userName = userName;
    }

    /** Запускает опрос, возвращает первый вопрос. */
    public String start() {
        state = State.WAITING_AGE;
        return "Привет, " + userName + "! Давай познакомимся. Сколько тебе лет?";
    }

    /** Активен ли сейчас опрос. */
    public boolean isActive() {
        return state != State.NONE;
    }

    /**
     * Обрабатывает ввод пользователя.
     * Возвращает следующий вопрос или итоговое сообщение.
     *
     * @param input      текст от пользователя
     * @param onComplete вызывается когда профиль собран
     */
    // Consumer - это способ передачи метода в аргумент
    public String handle(String input, Consumer<UserProfile> onComplete) {
        if (state == State.WAITING_AGE) {
            pendingAge = input.trim();
            state = State.WAITING_CITY;
            return "Из какого ты города?";

        } else if (state == State.WAITING_CITY) {
            int age = 0;
            try {
                age = Integer.parseInt(pendingAge.trim());
            } catch (NumberFormatException ignored) {}

            UserProfile profile = new UserProfile(userName, age, input.trim());
            state = State.NONE;
            pendingAge = null;

            onComplete.accept(profile);
            return "Запомнил! " + age + " лет, город — " + input.trim() + ".";
        }

        return null;
    }
}