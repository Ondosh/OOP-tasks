package com.github.ondosh.chatbot.util;

import java.io.*;

/**
 * Утилитарный класс для управления статистикой чата.
 * Отвечает за сохранение, загрузку и очистку статистики сообщений.
 *
 * <p>Статистика сохраняется в файл stats.txt в формате:
 * <pre>
 * Имя пользователя
 * Общее_количество|Количество_сообщений_пользователя|Количество_сообщений_бота
 * </pre>
 *
 * <p>Пример содержимого файла:
 * <pre>
 * Анна
 * 25|12|13
 * </pre>
 */
public class StatsManager {

    /** Имя файла для хранения статистики. */
    private static final String STATS_FILE = "stats.txt";

    /**
     * Сохраняет статистику чата в файл.
     *
     * <p>Формат сохранения:
     * <ul>
     *   <li>Первая строка — имя пользователя</li>
     *   <li>Вторая строка — три числа, разделённые символом '|'</li>
     * </ul>
     *
     * @param userName имя пользователя (используется для проверки принадлежности статистики)
     * @param total    общее количество сообщений
     * @param user     количество сообщений от пользователя
     * @param bot      количество сообщений от бота
     */
    public static void saveStats(String userName, int total, int user, int bot) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(STATS_FILE))) {
            writer.write(userName);
            writer.newLine();
            writer.write(total + "|" + user + "|" + bot);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения статистики: " + e.getMessage());
        }
    }

    /**
     * Загружает статистику из файла.
     *
     * <p>Если файл не существует или повреждён, возвращается статистика,
     * инициализированная нулями.
     *
     * @return массив из трёх целых чисел: [общее_количество, сообщения_пользователя, сообщения_бота]
     */
    public static int[] loadStats() {
        File file = new File(STATS_FILE);
        if (!file.exists()) {
            return new int[]{0, 0, 0};
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Пропускаем строку с именем пользователя
            reader.readLine();
            String statsLine = reader.readLine();

            if (statsLine != null) {
                String[] parts = statsLine.split("\\|");
                return new int[]{
                        Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2])
                };
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Ошибка загрузки статистики: " + e.getMessage());
        }

        return new int[]{0, 0, 0};
    }

    /**
     * Очищает файл статистики, удаляя все сохранённые данные.
     * Используется при создании нового профиля пользователя.
     */
    public static void clearStats() {
        File file = new File(STATS_FILE);
        if (file.exists()) {
            file.delete();
        }
    }
}