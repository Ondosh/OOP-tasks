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
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Сохраняет историю, заменяя переносы строк на маркеры.
     */
    public static void save(User user) {
        if (user == null) return;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            writer.write("USER:" + user.getName());
            writer.newLine();

            for (Message message : user.getHistory()) {
                // Заменяем переносы строк на HTML-подобные маркеры
                String escapedText = message.getText()
                        .replace("\n", "&#10;")
                        .replace("\r", "&#13;");

                writer.write(
                        message.getSender().name() + "|" +
                                message.getAuthor() + "|" +
                                message.getTime().format(FORMATTER) + "|" +
                                escapedText
                );
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Ошибка сохранения истории: " + e.getMessage());
        }
    }

    /**
     * Загружает историю, восстанавливая переносы строк из маркеров.
     */
    public static List<Message> load(String userName) {
        List<Message> history = new ArrayList<>();
        File file = new File(FILE_NAME);

        if (!file.exists()) return history;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String firstLine = reader.readLine();

            if (firstLine == null || !firstLine.equals("USER:" + userName)) {
                return history;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", 4);
                if (parts.length < 4) continue;

                Message.Sender sender = Message.Sender.valueOf(parts[0]);
                String author = parts[1];

                // Восстанавливаем переносы строк из маркеров
                String text = parts[3]
                        .replace("&#10;", "\n")
                        .replace("&#13;", "\r");

                history.add(new Message(author, text, sender));
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки истории: " + e.getMessage());
        }

        return history;
    }
}