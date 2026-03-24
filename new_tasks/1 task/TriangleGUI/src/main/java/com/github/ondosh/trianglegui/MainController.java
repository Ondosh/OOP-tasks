package com.github.ondosh.trianglegui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.github.ondosh.Triangle;

public class MainController {
    @FXML private TextField side1Field;
    @FXML private TextField side2Field;
    @FXML private TextField side3Field;
    @FXML private Label perimeterLabel;
    @FXML private Label areaLabel;
    @FXML private Label errorLabel; // Label для ошибок

    private Triangle triangle;

    @FXML
    public void initialize() {
        triangle = new Triangle(3, 4, 5);
        side1Field.setText("3");
        side2Field.setText("4");
        side3Field.setText("5");
        updateDisplay();
        clearError();
    }

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
            // Здесь NumberFormatException - тип исключения, а е - просто имя переменной
            // через которую получаем доступ к объекту ошибки.
            showError("Ошибка: введите числа");
        } catch (IllegalArgumentException e) {
            showError("Ошибка: " + e.getMessage());
            // Здесь е.getMessage применяется для того, чтобы узнать что именно за ошибку мы поймали.
        }
    }

    private void updateDisplay() {
        perimeterLabel.setText(String.format("%.2f", triangle.getPerimeter()));
        areaLabel.setText(String.format("%.2f", triangle.getArea()));
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void clearError() {
        errorLabel.setVisible(false);
    }

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