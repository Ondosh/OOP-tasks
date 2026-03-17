package com.github.ondosh.chatbot.util;

import com.github.ondosh.chatbot.model.Message;
import com.github.ondosh.chatbot.model.User;

import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HistoryManager {

    private static final String FILE_NAME = "chat_history.txt";
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    // Сохраняем историю при закрытии
    public static void save(User user) {
        if (user == null) return;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            // Первая строка — имя пользователя
            writer.write("USER:" + user.getName());
            writer.newLine();

            for (Message message : user.getHistory()) {
                // Формат строки: SENDER|AUTHOR|HH:mm|текст
                writer.write(
                        message.getSender().name() + "|" +
                                message.getAuthor()        + "|" +
                                message.getTime().format(FORMATTER) + "|" +
                                message.getText()
                );
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Ошибка сохранения истории: " + e.getMessage());
        }
    }

    // Загружаем историю при запуске
    public static List<Message> load(String userName) {
        List<Message> history = new ArrayList<>();
        File file = new File(FILE_NAME);

        if (!file.exists()) return history;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String firstLine = reader.readLine();

            // Проверяем что история принадлежит этому пользователю
            if (firstLine == null || !firstLine.equals("USER:" + userName)) {
                return history;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", 4);
                if (parts.length < 4) continue;

                Message.Sender sender = Message.Sender.valueOf(parts[0]);
                String author = parts[1];
                LocalTime time = LocalTime.parse(parts[2], FORMATTER);
                String text = parts[3];

                history.add(new Message(author, text, sender));
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки истории: " + e.getMessage());
        }

        return history;
    }
}