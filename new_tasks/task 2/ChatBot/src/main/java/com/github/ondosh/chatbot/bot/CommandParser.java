package com.github.ondosh.chatbot.bot;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CommandParser {

    // Статистика сообщений
    private int totalMessages = 0;
    private int userMessages  = 0;
    private int botMessages   = 1;
    // Сообщения бота сразу начинаются с единицы, т.к. бот пишет приветственное сообщение

    public void countUserMessage() { userMessages++;  totalMessages++; }
    public void countBotMessage()  { botMessages++;   totalMessages++; }

    public boolean isCommand(String input) {
        String lower = input.toLowerCase().trim();
        return lower.contains("который час")
                || lower.contains("сколько времени")
                || lower.contains("какое время")
                || lower.contains("какая дата")
                || lower.contains("какой день")
                || lower.contains("статистика")
                || lower.contains("сколько сообщений")
                || lower.matches(".*умножь.*на.*")
                || lower.matches(".*раздели.*на.*")
                || lower.matches(".*сложи.*и.*")
                || lower.matches(".*вычти.*из.*");
    }

    public String executeCommand(String input) {
        String lower = input.toLowerCase().trim();

        // Время
        if (lower.contains("который час") || lower.contains("сколько времени")
                || lower.contains("какое время")) {
            return "Сейчас " + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("HH:mm"));
        }

        // Дата
        if (lower.contains("какая дата") || lower.contains("какой день")) {
            return "Сегодня " + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy, EEEE"));
        }

        // Статистика
        if (lower.contains("статистика") || lower.contains("сколько сообщений")) {
            return String.format(
                    "Статистика чата:\n" +
                            "Всего сообщений: %d\n" +
                            "Твоих сообщений: %d\n" +
                            "Моих сообщений: %d",
                    totalMessages, userMessages, botMessages
            );
        }

        // Умножение: "умножь 12 на 157"
        if (lower.matches(".*умножь.*на.*")) {
            double[] nums = extractTwoNumbers(lower, "умножь", "на");
            if (nums != null) {
                return formatResult(nums[0] * nums[1]);
            }
        }

        // Деление: "раздели 100 на 4"
        if (lower.matches(".*раздели.*на.*")) {
            double[] nums = extractTwoNumbers(lower, "раздели", "на");
            if (nums != null) {
                if (nums[1] == 0) return "На ноль делить нельзя!";
                return formatResult(nums[0] / nums[1]);
            }
        }

        // Сложение: "сложи 15 и 27"
        if (lower.matches(".*сложи.*и.*")) {
            double[] nums = extractTwoNumbers(lower, "сложи", "и");
            if (nums != null) {
                return formatResult(nums[0] + nums[1]);
            }
        }

        // Вычитание: "вычти 5 из 20"
        if (lower.matches(".*вычти.*из.*")) {
            double[] nums = extractTwoNumbers(lower, "вычти", "из");
            if (nums != null) {
                // "вычти 5 из 20" → 20 - 5
                return formatResult(nums[1] - nums[0]);
            }
        }

        return "Не удалось выполнить команду.";
    }

    // Извлекаем два числа из строки вида "команда ЧИСЛО разделитель ЧИСЛО"
    private double[] extractTwoNumbers(String input, String command, String separator) {
        try {
            // Убираем часть до команды
            String after = input.substring(
                    input.indexOf(command) + command.length()
            ).trim();

            // Разбиваем по разделителю
            String[] parts = after.split(separator, 2);
            if (parts.length < 2) return null;

            double a = Double.parseDouble(parts[0].trim());
            double b = Double.parseDouble(parts[1].trim());
            return new double[]{a, b};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Красивый вывод — целое число если нет дробной части
    private String formatResult(double result) {
        if (result == Math.floor(result) && !Double.isInfinite(result)) {
            return "Результат: " + (long) result;
        }
        return "Результат: " + result;
    }
}