package com.github.ondosh.database.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DatabaseManager {
    public static Connection connection;

    // Чтение пароля из файла
    private static String getPasswordFromFile() {
        try {
            // Файл должен лежать в корне проекта или укажи полный путь
            String password = Files.readString(Paths.get("pswd.txt")).trim();
            System.out.println("Пароль загружен из файла");
            return password;
        } catch (Exception e) {
            System.out.println("Не удалось прочитать файл pswd.txt: " + e.getMessage());
            return ""; // или верни пароль по умолчанию
        }
    }

    // Только подключение
    public static Connection getConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    return connection;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        String url = "jdbc:postgresql://localhost:5432/OOP";
        String user = "postgres";
        String password = getPasswordFromFile(); // Читаем пароль из файла

        try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Подключение успешно!");
            return connection;
        } catch (SQLException e) {
            System.out.println("Ошибка подключения: " + e.getMessage());
            return null;
        }
    }

    // Закрытие соединения
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Соединение закрыто");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}