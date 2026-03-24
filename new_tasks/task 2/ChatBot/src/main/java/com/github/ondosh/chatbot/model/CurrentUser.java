package com.github.ondosh.chatbot.model;

public class CurrentUser {

    private static User user;
    private static UserProfile profile;

    public static void set(User u)          { user = u; }
    public static User get()                { return user; }

    public static void setProfile(UserProfile p) { profile = p; }
    public static UserProfile getProfile()       { return profile; }
}