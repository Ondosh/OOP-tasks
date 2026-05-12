package com.github.ondosh.database.DAO;

import com.github.ondosh.database.model.Game;

import java.util.List;

public interface DAO {
    // Добавляем игру в таблицу
    void addGame(Game game);

    // Получаем объект по его ID
    Game getGameByID(int id);

    // Получаем всю таблицу, чтобы можно было занести её в GUI
    List<Game> getAllGames();

    // Изменяем запись, передавая объект с новыми полями и ID для замены конкре
    void updateGame(Game game, int id);

    void deleteGame(int id);

    List<Game> getSortedBy(String field); // сортировка по полю
}
