package com.github.ondosh;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для класса Triangle.
 */
public class TriangleTest {

    private Triangle triangle;

    @BeforeEach // Делает выполнение перед каждым новым тестом
    void setUp() {
        // Выполняется перед каждым тестом - создаем свежий треугольник 3-4-5
        // Чтобы тесты не зависели друг от друга
        triangle = new Triangle(3, 4, 5);
    }

    // ========== ТЕСТЫ КОНСТРУКТОРОВ ==========

    @Test
    void testConstructorWithSides() {
        assertEquals(3, triangle.GetSide1());
        assertEquals(4, triangle.GetSide2());
        assertEquals(5, triangle.GetSide3());
    }

    @Test
    void testConstructorWithCoordinates() {
        // Треугольник с вершинами (0,0), (3,0), (0,4)
        Triangle t = new Triangle(0, 0, 3, 0, 0, 4);

        // Расстояния между точками:
        // (0,0) -> (3,0) = 3
        // (3,0) -> (0,4) = 5 (расстояние между (3,0) и (0,4))
        // (0,4) -> (0,0) = 4

        assertEquals(3, t.GetSide1(), 0.001);
        assertEquals(5, t.GetSide2(), 0.001);  // Здесь должно быть 5, а не 4!
        assertEquals(4, t.GetSide3(), 0.001);
    }

    // ========== ТЕСТЫ ИСКЛЮЧЕНИЙ ==========

    @ParameterizedTest
    @CsvSource({
            "0, 4, 5",
            "3, 0, 5",
            "3, 4, 0",
            "-1, 4, 5",
            "3, -2, 5",
            "3, 4, -3"
    })
    void testInvalidSidesThrowException(double a, double b, double c) {
        // @ParameterizedTest - тест будет выполнен несколько раз с разными данными
        // @CsvSource - источник данных в формате CSV (значения разделены запятыми)
        assertThrows(IllegalArgumentException.class, () -> {
            // Первый аргумент (IllegalArgumentException.class) - ожидаемый тип исключения
            // Второй аргумент (() -> { ... }) - анонимная функция без параметров,
            new Triangle(a, b, c); // Эти значения a,b,c приходят из CSV строки
        });
    }

    @Test
    void testInvalidTriangleInequality() {
        // Нарушение неравенства треугольника: 1 + 2 < 4
        assertThrows(IllegalArgumentException.class, () -> {
            new Triangle(1, 2, 4);
        });
    }

    @Test
    void testCollinearPointsThrowException() {
        // Точки на одной прямой (0,0), (1,1), (2,2)
        assertThrows(IllegalArgumentException.class, () -> {
            new Triangle(0, 0, 1, 1, 2, 2);
        });
    }

    // ========== ТЕСТЫ ГЕТТЕРОВ И СЕТТЕРОВ ==========

    @Test
    void testGetters() {
        assertEquals(3, triangle.GetSide1());
        assertEquals(4, triangle.GetSide2());
        assertEquals(5, triangle.GetSide3());
    }

    @Test
    void testSetters() {
        triangle.SetSide1(5);
        triangle.SetSide2(6);
        triangle.SetSide3(7);

        assertEquals(5, triangle.GetSide1());
        assertEquals(6, triangle.GetSide2());
        assertEquals(7, triangle.GetSide3());
    }

    @Test
    void testSetAll() {
        triangle.SetAll(6, 7, 8);

        assertEquals(6, triangle.GetSide1());
        assertEquals(7, triangle.GetSide2());
        assertEquals(8, triangle.GetSide3());
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1, 0, -5})
    void testInvalidSetterThrowsException(double invalidValue) {
        assertThrows(IllegalArgumentException.class, () -> {
            triangle.SetSide1(invalidValue);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            triangle.SetSide2(invalidValue);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            triangle.SetSide3(invalidValue);
        });
    }

    // ========== ТЕСТЫ ПЕРИМЕТРА ==========

    @Test
    void testPerimeter() {
        assertEquals(12, triangle.getPerimeter());
    }

    @ParameterizedTest
    @CsvSource({
            "3, 4, 5, 12",
            "5, 5, 5, 15",
            "4, 4, 3, 11",
            "2, 3, 4, 9"
    })
    void testPerimeterWithDifferentTriangles(double a, double b, double c, double expectedPerimeter) {
        Triangle t = new Triangle(a, b, c);
        assertEquals(expectedPerimeter, t.getPerimeter());
    }

    // ========== ТЕСТЫ ПЛОЩАДИ ==========

    @Test
    void testArea() {
        assertEquals(6.0, triangle.getArea(), 0.001);
    }

    @ParameterizedTest
    @CsvSource({
            "3, 4, 5, 6.0",
            "5, 5, 5, 10.825",
            "4, 4, 3, 5.562",
            "2, 3, 4, 2.905"
    })
    void testAreaWithDifferentTriangles(double a, double b, double c, double expectedArea) {
        Triangle t = new Triangle(a, b, c);
        assertEquals(expectedArea, t.getArea(), 0.001);
    }

    // ========== ТЕСТЫ МЕТОДА TOSTRING ==========

    @Test
    void testToString() {
        String result = triangle.toString();
        // В методе toString используется форматирование с двумя знаками после запятой
        assertTrue(result.contains("3,00") || result.contains("3.00")); // С учетом локали
        assertTrue(result.contains("4,00") || result.contains("4.00"));
        assertTrue(result.contains("5,00") || result.contains("5.00"));
    }

    // ========== ДОПОЛНИТЕЛЬНЫЕ ТЕСТЫ ==========

    @Test
    void testEquilateralTriangle() {
        Triangle equilateral = new Triangle(5, 5, 5);
        double area = equilateral.getArea();
        double expected = (Math.sqrt(3) / 4) * 25; // Формула для равностороннего треугольника
        assertEquals(expected, area, 0.001);
    }

    @Test
    void testRightTriangle() {
        // Проверяем, что для прямоугольного треугольника работает теорема Пифагора
        double a = triangle.GetSide1();
        double b = triangle.GetSide2();
        double c = triangle.GetSide3();

        assertEquals(c * c, a * a + b * b, 0.001);
    }

    @Test
    void testVerySmallTriangle() {
        Triangle small = new Triangle(0.1, 0.1, 0.1);
        assertEquals(0.3, small.getPerimeter(), 0.001);
        assertTrue(small.getArea() > 0);
    }
}