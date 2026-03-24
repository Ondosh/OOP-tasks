package com.github.ondosh.chatbot.util;

import com.github.ondosh.chatbot.model.UserProfile;

import java.io.*;

public class ProfileManager {

    private static final String FILE_NAME = "user_profile.txt";

    // Формат файла:
    // NAME:Иван
    // AGE:20
    // CITY:Москва

    public static void save(UserProfile profile) {
        if (profile == null) return;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            writer.write("NAME:" + profile.getName()); writer.newLine();
            writer.write("AGE:"  + profile.getAge());  writer.newLine();
            writer.write("CITY:" + profile.getCity()); writer.newLine();
        } catch (IOException e) {
            System.err.println("Ошибка сохранения профиля: " + e.getMessage());
        }
    }

    // Возвращает null, если файл не найден или имя не совпадает
    public static UserProfile load(String userName) {
        File file = new File(FILE_NAME);
        if (!file.exists()) return null;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String name = parseLine(reader.readLine(), "NAME:");
            String age  = parseLine(reader.readLine(), "AGE:");
            String city = parseLine(reader.readLine(), "CITY:");

            if (name == null || !name.equals(userName)) return null;

            return new UserProfile(name, Integer.parseInt(age), city);

        } catch (Exception e) {
            System.err.println("Ошибка загрузки профиля: " + e.getMessage());
            return null;
        }
    }

    private static String parseLine(String line, String prefix) {
        if (line == null || !line.startsWith(prefix)) return null;
        return line.substring(prefix.length());
    }
}