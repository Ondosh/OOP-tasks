package com.github.ondosh.database.controller;

import com.github.ondosh.database.DAO.GameDAO;
import com.github.ondosh.database.model.Game;
import com.github.ondosh.database.service.GameService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class MainController {

    // Элементы из fxml
    @FXML private TableView<Game> gamesTable;
    @FXML private TableColumn<Game, String> titleColumn;
    @FXML private TableColumn<Game, String> genreColumn;
    @FXML private TableColumn<Game, Float> priceColumn;
    @FXML private TableColumn<Game, Float> ratingColumn;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private Label statusLabel;

    // Данные хранятся здесь, не в таблице
    private final ObservableList<Game> gamesList = FXCollections.observableArrayList();

    private final GameDAO gameDAO = new GameDAO();
    private final GameService gameService = new GameService();

    /**
     * Вызывается автоматически при запуске — инициализация таблицы
     */
    @FXML
    public void initialize() {
        System.out.println("контроллер вызван");
        // Привязываем колонки к полям класса Game
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));

        // Привязываем список к таблице
        gamesTable.setItems(gamesList);

        // Заполняем выпадающий список сортировки
        sortComboBox.setItems(FXCollections.observableArrayList(
                "title", "genre", "price", "rating"
        ));

        // Загружаем данные из БД
        loadGames();
    }

    /**
     * Загружает все игры из БД в список
     */
    private void loadGames() {
        gamesList.clear();
        List<Game> games = gameDAO.getAllGames();

        System.out.println("Получено игр: " + games.size()); // добавь
        for (Game g : games) {
            System.out.println(g); // добавь — выведет toString()
        }

        gamesList.addAll(games);
        statusLabel.setText("Загружено игр: " + gamesList.size());
    }

    /**
     * Нажатие кнопки Добавить
     */
    @FXML
    private void onAddButton() {
        // Пока просто заглушка — потом сделаем диалоговое окно
        statusLabel.setText("Добавить — в разработке");
    }

    /**
     * Нажатие кнопки Изменить
     */
    @FXML
    private void onEditButton() {
        Game selected = gamesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Выберите игру для изменения");
            return;
        }
        // Пока заглушка
        statusLabel.setText("Изменить — в разработке");
    }

    /**
     * Нажатие кнопки Удалить
     */
    @FXML
    private void onDeleteButton() {
        Game selected = gamesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Выберите игру для удаления");
            return;
        }

        gameDAO.deleteGame(selected.getId());
        gamesList.remove(selected);
        statusLabel.setText("Игра удалена: " + selected.getTitle());
    }

    /**
     * Изменение сортировки
     */
    @FXML
    private void onSortChanged() {
        String field = sortComboBox.getValue();
        if (field == null) return;

        gamesList.clear();
        gamesList.addAll(gameDAO.getSortedBy(field));
        statusLabel.setText("Сортировка по: " + field);
    }
}