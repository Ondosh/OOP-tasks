package com.github.ondosh.chatbot.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Модель пользователя. Хранит имя и историю сообщений текущей сессии.
 */
public class User {

    /** Имя пользователя, задаётся при создании и не меняется. */
    private final String name;

    /** История сообщений в оперативной памяти. */
    private final List<Message> history = new ArrayList<>();

    /**
     * конструктор
     * @param name - имя пользователя
     */
    public User(String name) {
        this.name = name;
    }

    /**
     * Геттер имени пользователя
     * @return name
     */
    public String getName() { return name; }

    /** Добавляет сообщение в историю. */
    public void addMessage(Message message) {
        history.add(message);
    }

    /**
     * Возвращает историю только для чтения —
     * внешний код не может изменить список напрямую.
     */
    public List<Message> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public int getMessageCount() {
        return history.size();
    }
}