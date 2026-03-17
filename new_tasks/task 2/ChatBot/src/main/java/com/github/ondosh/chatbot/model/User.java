package com.github.ondosh.chatbot.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class User {

    private final String name;
    private final List<Message> history = new ArrayList<>();

    public User(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public void addMessage(Message message) {
        history.add(message);
    }

    public List<Message> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public int getMessageCount() {
        return history.size();
    }
}