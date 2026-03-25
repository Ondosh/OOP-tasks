package com.github.ondosh.chatbot.util;

import com.github.ondosh.chatbot.model.Message;
import com.github.ondosh.chatbot.model.User;

import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Утилитный класс для сохранения и загрузки истории сообщений.
 * История хранится в текстовом файле и привязана к имени пользователя —
 * при смене имени история не загружается.
 */
public class HistoryManager {

    /** Имя файла для хранения истории сообщений. */
    private static final String FILE_NAME = "chat_history.txt";

    /**
     * Формат времени для записи и чтения сообщений.
     * Используется единый формат, чтобы save() и load() были совместимы.
     */
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Сохраняет всю историю сообщений пользователя в файл.
     * Первая строка — имя пользователя для идентификации при загрузке.
     * Каждое сообщение записывается в формате: SENDER|AUTHOR|HH:mm|текст
     *
     * @param user пользователь, чья история сохраняется; если null — метод ничего не делает
     */
    public static void save(User user) {
        if (user == null) return;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            // Первая строка — имя пользователя, по ней load() проверяет принадлежность файла
            writer.write("USER:" + user.getName());
            writer.newLine();

            for (Message message : user.getHistory()) {
                // Формат строки: SENDER|AUTHOR|HH:mm|текст
                // Разделитель | выбран, так как маловероятен в обычном тексте
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

    /**
     * Загружает историю сообщений из файла для указанного пользователя.
     *
     * Возвращает {@link List} сообщений — упорядоченную коллекцию,
     * которая сохраняет порядок записей из файла (то есть хронологию чата).
     * Если файл не найден или принадлежит другому пользователю — возвращает пустой список.
     *
     * @param userName имя пользователя для проверки принадлежности файла
     * @return список сообщений в порядке их записи; пустой список если история не найдена
     */
    public static List<Message> load(String userName) {
        List<Message> history = new ArrayList<>();
        File file = new File(FILE_NAME);

        if (!file.exists()) return history;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String firstLine = reader.readLine();

            // Если файл пустой или принадлежит другому пользователю — не загружаем
            if (firstLine == null || !firstLine.equals("USER:" + userName)) {
                return history;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                // Разбиваем строку на 4 части по разделителю |
                // Лимит 4 защищает от разбиения текста сообщения, если в нём есть символ |
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