package com.github.ondosh.database.service;

import com.github.ondosh.database.DAO.GameDAO;
import com.github.ondosh.database.model.Game;

/**
 * Сервисный слой — только валидация данных перед записью в БД.
 */
public class GameService {

    private final GameDAO gameDAO;

    public GameService() {
        this.gameDAO = new GameDAO();
    }

    /**
     * Валидация объекта Game.
     * Проверяет все поля на корректность.
     */
    private void validate(Game game) {
        if (game.getTitle() == null || game.getTitle().isBlank())
            throw new IllegalArgumentException("Название не может быть пустым");

        if (game.getGenre() == null || game.getGenre().isBlank())
            throw new IllegalArgumentException("Жанр не может быть пустым");

        if (game.getPrice() < 0)
            throw new IllegalArgumentException("Цена не может быть отрицательной");

        if (game.getRating() < 0 || game.getRating() > 10)
            throw new IllegalArgumentException("Рейтинг должен быть от 0 до 10");
    }

    // С проверкой
    public void addGame(Game game) {
        validate(game);
        gameDAO.addGame(game);
    }

    // С проверкой
    public void updateGame(Game game, int id) {
        validate(game);
        gameDAO.updateGame(game, id);
    }
}