package com.github.ondosh.trianglegui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.github.ondosh.Triangle;

/**
 * Контроллер главного окна приложения.
 * Обрабатывает ввод пользователя, управляет объектом {@link Triangle}
 * и обновляет отображение результатов.
 */
public class MainController {

    /** Поле ввода первой стороны треугольника. */
    @FXML private TextField side1Field;

    /** Поле ввода второй стороны треугольника. */
    @FXML private TextField side2Field;

    /** Поле ввода третьей стороны треугольника. */
    @FXML private TextField side3Field;

    /** Метка для отображения периметра. */
    @FXML private Label perimeterLabel;

    /** Метка для отображения площади. */
    @FXML private Label areaLabel;

    /** Метка для отображения сообщений об ошибках. */
    @FXML private Label errorLabel;

    /** Объект треугольника, над которым выполняются вычисления. */
    private Triangle triangle;

    /**
     * Вызывается автоматически после загрузки FXML-разметки.
     * Создаёт треугольник со сторонами по умолчанию (3, 4, 5),
     * заполняет поля ввода и выводит начальные результаты.
     */
    @FXML
    public void initialize() {
        triangle = new Triangle(3, 4, 5);
        side1Field.setText("3");
        side2Field.setText("4");
        side3Field.setText("5");
        updateDisplay();
        clearError();
    }

    /**
     * Считывает стороны из полей ввода, пересчитывает параметры треугольника
     * и обновляет метки результатов.
     * <p>
     * Вызывается при нажатии кнопки «Вычислить».
     * При некорректном вводе выводит сообщение об ошибке.
     */
    @FXML
    private void calculateTriangle() {
        try {
            double a = Double.parseDouble(side1Field.getText());
            double b = Double.parseDouble(side2Field.getText());
            double c = Double.parseDouble(side3Field.getText());

            triangle.SetAll(a, b, c);
            updateDisplay();
            clearError();

        } catch (NumberFormatException e) {
            // Пользователь ввёл нечисловое значение — сообщаем об этом
            showError("Ошибка: введите числа");
        } catch (IllegalArgumentException e) {
            // Triangle выбросил исключение, если стороны не образуют треугольник
            showError("Ошибка: " + e.getMessage());
        }
    }

    /**
     * Обновляет метки периметра и площади значениями из текущего объекта треугольника.
     * Числа форматируются с двумя знаками после запятой.
     */
    private void updateDisplay() {
        perimeterLabel.setText(String.format("%.2f", triangle.getPerimeter()));
        areaLabel.setText(String.format("%.2f", triangle.getArea()));
    }

    /**
     * Показывает сообщение об ошибке в соответствующей метке.
     *
     * @param message текст ошибки для отображения пользователю
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    /**
     * Скрывает метку с сообщением об ошибке.
     */
    private void clearError() {
        errorLabel.setVisible(false);
    }

    /**
     * Очищает поля ввода, сбрасывает метки результатов и скрывает ошибку.
     * <p>
     * Вызывается при нажатии кнопки «Очистить».
     */
    @FXML
    private void clearFields() {
        side1Field.clear();
        side2Field.clear();
        side3Field.clear();
        perimeterLabel.setText("");
        areaLabel.setText("");
        clearError();
    }
}