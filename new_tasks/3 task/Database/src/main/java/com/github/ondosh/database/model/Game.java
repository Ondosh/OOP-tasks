package com.github.ondosh.database.model;

public class Game {
    private int id;
    private String title;
    private String genre;
    private float price;
    private float rating;

    // Конструктор по умолчанию будет лишним

    // Конструктор со всеми полями (без id, для добавления новой записи)
    public Game(String title, String genre, float price, float rating) {
        this.title = title;
        this.genre = genre;
        this.price = price;
        this.rating = rating;
    }

    // Конструктор с id (для обновления существующих записей) (загрузка с базы данных)
    public Game(int id, String title, String genre, float price, float rating) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.price = price;
        this.rating = rating;
    }

    // Геттеры и сеттеры
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    // Переопределение toString() для удобного вывода
    @Override
    public String toString() {
        return "Game{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", genre='" + genre + '\'' +
                ", price=" + price +
                ", rating=" + rating +
                '}';
    }
}