package com.github.ondosh.chatbot.model;

/**
 * Профиль пользователя с персональными данными.
 * Сохраняется в файл через ProfileManager и передаётся в GigaChatBot
 * для персонализации системного промпта.
 */
public class UserProfile {

    /** Имя пользователя — совпадает с User.name, используется для идентификации файла. */
    private final String name;

    /** Возраст пользователя. */
    private int age;

    /** Город пользователя. */
    private String city;

    /** Конструктор класса*/
    public UserProfile(String name, int age, String city) {
        this.name = name;
        this.age  = age;
        this.city = city;
    }

    /** Метод возвращающий имя*/
    public String getName() { return name; }
    public int    getAge()  { return age;  }
    public String getCity() { return city; }
}