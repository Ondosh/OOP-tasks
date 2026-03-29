package com.github.ondosh.chatbot.util;

import com.github.ondosh.chatbot.model.UserProfile;

import java.io.*;

/**
 * Утилитный класс для сохранения и загрузки профиля пользователя.
 * Профиль хранится в текстовом файле и привязан к имени пользователя —
 * при смене имени профиль не загружается.
 *
 * Формат файла:
 * NAME:Иван
 * AGE:20
 * CITY:Москва
 */
public class ProfileManager {

    /** Имя файла для хранения профиля пользователя. */
    private static final String FILE_NAME = "user_profile.txt";

    /**
     * Сохраняет профиль пользователя в файл.
     * Каждое поле записывается на отдельной строке в формате КЛЮЧ:значение.
     *
     * @param profile профиль для сохранения; если null — метод ничего не делает
     */
    public static void save(UserProfile profile) {
        if (profile == null) return;

        // try-with-resources гарантирует закрытие файла даже при исключении
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            writer.write("NAME:" + profile.getName()); writer.newLine();
            writer.write("AGE:"  + profile.getAge());  writer.newLine();
            writer.write("CITY:" + profile.getCity()); writer.newLine();
        } catch (IOException e) {
            // IOException возникает при проблемах с файловой системой:
            // нет прав на запись, диск заполнен, файл заблокирован другим процессом и т.д.
            // Не бросаем исключение дальше — потеря профиля не критична для работы приложения
            System.err.println("Ошибка сохранения профиля: " + e.getMessage());
        }
    }

    /**
     * Загружает профиль пользователя из файла.
     * Если файл не найден или принадлежит другому пользователю — возвращает null,
     * что сигнализирует ChatController о необходимости запустить опрос профиля.
     *
     * @param userName имя пользователя для проверки принадлежности файла
     * @return загруженный профиль или null если профиль не найден
     */
    public static UserProfile load(String userName) {
        File file = new File(FILE_NAME);
        if (!file.exists()) return null;

        // Exception (а не IOException) — ловим широко, так как здесь возможны также:
        // NumberFormatException при парсинге возраста, NullPointerException если файл неполный
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String name = parseLine(reader.readLine(), "NAME:");
            String age  = parseLine(reader.readLine(), "AGE:");
            String city = parseLine(reader.readLine(), "CITY:");

            // Если имя не совпадает — файл принадлежит другому пользователю
            if (name == null || !name.equals(userName)) return null;

            return new UserProfile(name, Integer.parseInt(age), city);

        } catch (Exception e) {
            System.err.println("Ошибка загрузки профиля: " + e.getMessage());
            return null;
        }
    }

    /**
     * Извлекает значение из строки формата «КЛЮЧ:значение».
     * Возвращает null если строка пустая или не начинается с ожидаемого префикса —
     * это позволяет load() корректно обработать повреждённый или чужой файл.
     *
     * @param line   строка из файла
     * @param prefix ожидаемый префикс, например «NAME:»
     * @return значение после префикса или null
     */
    private static String parseLine(String line, String prefix) {
        if (line == null || !line.startsWith(prefix)) return null;
        return line.substring(prefix.length());
    }
}