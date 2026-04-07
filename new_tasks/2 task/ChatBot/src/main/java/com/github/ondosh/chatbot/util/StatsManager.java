package com.github.ondosh.chatbot.util;

import java.io.*;

public class StatsManager {
    private static final String STATS_FILE = "stats.txt";

    public static void saveStats(String userName, int total, int user, int bot) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(STATS_FILE))) {
            writer.write(userName);
            writer.newLine();
            writer.write(total + "|" + user + "|" + bot);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения статистики: " + e.getMessage());
        }
    }

    public static int[] loadStats() {
        File file = new File(STATS_FILE);
        if (!file.exists()) {
            return new int[]{0, 0, 0};
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
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

    public static void clearStats() {
        File file = new File(STATS_FILE);
        if (file.exists()) {
            file.delete();
        }
    }
}