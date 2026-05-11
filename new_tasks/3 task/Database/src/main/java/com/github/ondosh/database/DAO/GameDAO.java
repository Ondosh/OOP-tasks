package com.github.ondosh.database.DAO;

import com.github.ondosh.database.model.Game;
import com.github.ondosh.database.model.DatabaseManager;
import javafx.scene.chart.PieChart;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.github.ondosh.database.model.DatabaseManager.connection;

public class GameDAO implements DAO {
    @Override
    public Game getGameByID(int id) {
        String sql = "SELECT * FROM games WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // первому аргументу присваем значение из переменной id
            pstmt.setInt(1, id);
            // запускаем запрос который призван только вернуть данные
            ResultSet rs = pstmt.executeQuery();

            // проверяем есть ли следующая строка из данных
            if (rs.next()) {
                return new Game(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("genre"),
                        rs.getFloat("price"),
                        rs.getFloat("rating")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    @Override
    public List<Game> getAllGames() {
        String sql = "SELECT * from games";
        List<Game> games = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // запускаем запрос который призван только вернуть данные
            ResultSet rs = pstmt.executeQuery();

            // проверяем есть ли следующая строка из данных
            while (rs.next()) {
                games.add(new Game(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("genre"),
                        rs.getFloat("price"),
                        rs.getFloat("rating")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return games;
    }

    @Override
    public void updateGame(Game game, int id) {
        String sql = "UPDATE games SET title = ?, genre = ?, price = ?, rating = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, game.getTitle());
            pstmt.setString(2, game.getGenre());
            pstmt.setFloat(3, game.getPrice());
            pstmt.setFloat(4, game.getRating());
            pstmt.setInt(5, id);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteGame(int id) {
        String sql = "DELETE FROM games WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addGame(Game game) {
        String sql = "INSERT INTO games (title, genre, price, rating) VALUES (?, ?, ?, ?)";

        // получаем подключение прямо здесь
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, game.getTitle());
            pstmt.setString(2, game.getGenre());
            pstmt.setFloat(3, game.getPrice());
            pstmt.setFloat(4, game.getRating());
            // запрос, который изменяет данные, а не возвращает
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                game.setId(rs.getInt(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
