package com.github.ondosh.chatbot.model;

public class UserProfile {

    private final String name;
    private int age;
    private String city;

    public UserProfile(String name, int age, String city) {
        this.name = name;
        this.age  = age;
        this.city = city;
    }

    public String getName() { return name; }
    public int    getAge()  { return age;  }
    public String getCity() { return city; }
}