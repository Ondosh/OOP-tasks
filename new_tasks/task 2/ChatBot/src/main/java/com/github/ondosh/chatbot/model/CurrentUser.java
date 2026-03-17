package com.github.ondosh.chatbot.model;

public class CurrentUser {

    private static User instance;

    public static void set(User user) { instance = user; }
    public static User get()          { return instance; }
}