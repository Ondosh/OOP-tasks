package com.github.ondosh.chatbot.model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Модель одного сообщения в чате.
 * Хранит автора, текст, время отправки и тип отправителя (пользователь или бот).
 */
public class Message {

    /** Имя автора сообщения (имя пользователя или «Бот»). */
    private final String author;

    /** Текст сообщения. */
    private final String text;

    /** Время создания сообщения — фиксируется в момент вызова конструктора. */
    private final LocalTime time;

    /**
     * Тип отправителя — используется в ChatController для выбора стиля отображения:
     * USER — зелёный пузырь справа, BOT — белый пузырь слева.
     */
    public enum Sender {
        USER, BOT
    }

    /** Тип отправителя данного сообщения. */
    private final Sender sender;

    /**
     * Создаёт сообщение и фиксирует текущее время.
     *
     * @param author имя автора
     * @param text   текст сообщения
     * @param sender тип отправителя
     */
    public Message(String author, String text, Sender sender) {
        this.author = author;
        this.text   = text;
        this.sender = sender;
        this.time   = LocalTime.now();
    }

    public String getAuthor()  { return author; }
    public String getText()    { return text; }
    public Sender getSender()  { return sender; }
    public LocalTime getTime()   { return time;   }

    /**
     * Форматирует сообщение для отображения в ListView.
     * Пример: «[14:35] Иван: Привет!»
     */
    @Override
    public String toString() {
        String timeStr = time.format(DateTimeFormatter.ofPattern("HH:mm"));
        return "[" + timeStr + "] " + author + ": " + text;
    }
}