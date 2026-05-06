package com.github.ondosh.database.model;

import com.sun.jdi.connect.spi.Connection;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static Connection connection;

    // Только подключение
    public static void setConnection() {
        String url = "jdbc:postgresql://localhost:5432/OOP";
        String user = "postgres";
        String password = "";

        try (java.sql.Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Подключение успешно!");
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
        // Только закрытие
    public static void closeConnection () {
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
