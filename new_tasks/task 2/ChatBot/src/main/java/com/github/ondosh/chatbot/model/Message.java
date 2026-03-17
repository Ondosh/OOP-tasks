package com.github.ondosh.chatbot.model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Message {

    private final String author;
    private final String text;
    private final LocalTime time;

    // Кто пишет сообщение — пользователь или бот
    public enum Sender {
        USER, BOT
    }

    private final Sender sender;

    public Message(String author, String text, Sender sender) {
        this.author = author;
        this.text = text;
        this.sender = sender;
        this.time = LocalTime.now();
    }

    public String getAuthor()  { return author; }
    public String getText()    { return text; }
    public Sender getSender()  { return sender; }
    public LocalTime getTime() { return time; }

    // Удобный метод для отображения в ListView
    @Override
    public String toString() {
        String timeStr = time.format(DateTimeFormatter.ofPattern("HH:mm"));
        return "[" + timeStr + "] " + author + ": " + text;
    }
}