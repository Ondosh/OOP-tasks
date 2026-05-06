package com.github.ondosh.chatbot.util;

import com.github.ondosh.chatbot.model.Message;
import com.github.ondosh.chatbot.model.User;

import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Утилитарный класс для управления историей сообщений чата.
 * Отвечает за сохранение и загрузку истории переписки в файл.
 *
 * <p>История сохраняется в файл chat_history.txt в формате:
 * <pre>
 * USER:Имя_пользователя
 * SENDER|AUTHOR|ВРЕМЯ|ТЕКСТ_СООБЩЕНИЯ
 * SENDER|AUTHOR|ВРЕМЯ|ТЕКСТ_СООБЩЕНИЯ
 * </pre>
 *
 * <p>Специальные маркеры для сохранения переносов строк:
 * <ul>
 *   <li>&#10; — заменяет символ \n (перевод строки)</li>
 *   <li>&#13; — заменяет символ \r (возврат каретки)</li>
 * </ul>
 *
 * <p>Пример содержимого файла:
 * <pre>
 * USER:Анна
 * USER|Анна|14:30|Привет!
 * BOT|Бот|14:31|Здравствуйте! Чем могу помочь?
 * USER|Анна|14:32|Как дела?&#10;Что нового?
 * </pre>
 */
public class HistoryManager {

    /** Имя файла для хранения истории чата. */
    private static final String FILE_NAME = "chat_history.txt";

    /** Форматтер для отображения времени сообщений (часы:минуты). */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Сохраняет историю сообщений пользователя в файл.
     *
     * <p>Перед сохранением заменяет символы переноса строки (CR, LF)
     * на HTML-подобные маркеры, чтобы избежать разрыва формата файла.
     *
     * @param user объект пользователя, содержащий историю сообщений
     */
    public static void save(User user) {
        if (user == null) return;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            // Сохраняем имя пользователя как идентификатор
            writer.write("USER:" + user.getName());
            writer.newLine();

            for (Message message : user.getHistory()) {
                // Заменяем переносы строк на маркеры для безопасного сохранения
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
     * Загружает историю сообщений для указанного пользователя из файла.
     *
     * <p>Проверяет, что имя пользователя в файле совпадает с запрошенным.
     * Восстанавливает оригинальные переносы строк из маркеров.
     *
     * @param userName имя пользователя, для которого загружается история
     * @return список сообщений пользователя (может быть пустым, если история не найдена или повреждена)
     */
    public static List<Message> load(String userName) {
        List<Message> history = new ArrayList<>();
        File file = new File(FILE_NAME);

        if (!file.exists()) return history;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String firstLine = reader.readLine();

            // Проверяем, что файл принадлежит текущему пользователю
            if (firstLine == null || !firstLine.equals("USER:" + userName)) {
                return history;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                // Разделяем строку на 4 части (максимум, чтобы текст мог содержать символ '|')
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