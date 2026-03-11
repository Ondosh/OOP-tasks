package com.github.ondosh;

/**
 * Класс, представляющий геометрическую фигуру "Треугольник".
 * <p>
 * Класс позволяет создавать треугольники двумя способами:
 * <ul>
 *   <li>По трём сторонам</li>
 *   <li>По координатам трёх вершин на плоскости</li>
 * </ul>
 *
 * <p>Пример использования:
 * <pre>
 * // Создание треугольника по трём сторонам
 * Triangle t1 = new Triangle(3, 4, 5);
 * System.out.println("Площадь: " + t1.getArea());
 * System.out.println("Периметр: " + t1.getPerimeter());
 *
 * // Создание треугольника по координатам вершин
 * Triangle t2 = new Triangle(0, 0, 4, 0, 0, 3);
 * System.out.println("Стороны: " + t2);
 * </pre>
 *
 * @version 1.0
 * @author Ondosh
 */
public class Triangle {

    /** Первая сторона треугольника */
    private double side1;

    /** Вторая сторона треугольника */
    private double side2;

    /** Третья сторона треугольника */
    private double side3;

    /**
     * Устанавливает значение первой стороны треугольника.
     *
     * @param a новое значение первой стороны (должно быть положительным)
     * @throws IllegalArgumentException если значение set_side1 меньше или равно 0
     */
    public void SetSide1(double a) {
        if (a <= 0) {
            throw new IllegalArgumentException("Некорректное значение. Должно быть положительным: " + a);
        }

        // Временно сохраняем новое значение для проверки
        double oldSide1 = this.side1;
        this.side1 = a;

        // Проверяем, можно ли построить треугольник с новыми сторонами
        if (!isValidTriangle(this.side1, this.side2, this.side3)) {
            this.side1 = oldSide1; // Возвращаем старое значение
            throw new IllegalArgumentException("Невозможно установить сторону " + a +
                    ": нарушение неравенства треугольника");
        }
    }

    /**
     * Устанавливает значение второй стороны треугольника.
     *
     * @param b новое значение второй стороны (должно быть положительным)
     * @throws IllegalArgumentException если значение b меньше или равно 0
     */
    public void SetSide2(double b) {
        if (b <= 0) {
            throw new IllegalArgumentException("Некорректное значение. Должно быть положительным: " + b);
        }

        double oldSide2 = this.side2;
        this.side2 = b;

        if (!isValidTriangle(this.side1, this.side2, this.side3)) {
            this.side2 = oldSide2;
            throw new IllegalArgumentException("Невозможно установить сторону " + b +
                    ": нарушение неравенства треугольника");
        }
    }

    /**
     * Устанавливает значение третьей стороны треугольника.
     *
     * @param c новое значение третьей стороны (должно быть положительным)
     * @throws IllegalArgumentException если значение "c" меньше или равно 0
     */
    public void SetSide3(double c) {
        if (c <= 0) {
            throw new IllegalArgumentException("Некорректное значение. Должно быть положительным: " + c);
        }

        double oldSide3 = this.side3;
        this.side3 = c;

        if (!isValidTriangle(this.side1, this.side2, this.side3)) {
            this.side3 = oldSide3;
            throw new IllegalArgumentException("Невозможно установить сторону " + c +
                    ": нарушение неравенства треугольника");
        }
    }

    /**
     * Возвращает значение первой стороны треугольника.
     *
     * @return текущее значение первой стороны
     */
    public double GetSide1() {
        return this.side1;
    }

    /**
     * Возвращает значение второй стороны треугольника.
     *
     * @return текущее значение второй стороны
     */
    public double GetSide2() {
        return this.side2;
    }

    /**
     * Возвращает значение третьей стороны треугольника.
     *
     * @return текущее значение третьей стороны
     */
    public double GetSide3() {
        return this.side3;
    }

    /**
     * Устанавливает все три стороны треугольника одновременно.
     *
     * @param a значение первой стороны
     * @param b значение второй стороны
     * @param c значение третьей стороны
     * @throws IllegalArgumentException если любое из значений меньше или равно 0
     */
    public void SetAll(double a, double b, double c) {
        // Проверяем все значения сразу
        if (a <= 0 || b <= 0 || c <= 0) {
            throw new IllegalArgumentException("Все стороны должны быть положительными");
        }

        if (!isValidTriangle(a, b, c)) {
            throw new IllegalArgumentException("Стороны не могут образовать треугольник");
        }

        // Если все проверки пройдены, устанавливаем новые значения
        this.side1 = a;
        this.side2 = b;
        this.side3 = c;
    }

    /**
     * Конструктор, создающий треугольник по трём сторонам.
     *
     * @param side1 длина первой стороны
     * @param side2 длина второй стороны
     * @param side3 длина третьей стороны
     * @throws IllegalArgumentException если:
     *         <ul>
     *           <li>любая сторона меньше или равна 0</li>
     *           <li>стороны не удовлетворяют неравенству треугольника</li>
     *         </ul>
     */
    public Triangle(double side1, double side2, double side3) {
        if (!isValidTriangle(side1, side2, side3)) {
            throw new IllegalArgumentException("Стороны не могут образовать треугольник. Нарушение неравенства треугольника.");
        }
        this.side1 = side1;
        this.side2 = side2;
        this.side3 = side3;
    }

    /**
     * Конструктор, создающий треугольник по координатам трёх вершин на плоскости.
     *
     * @param x1 координата X первой вершины
     * @param y1 координата Y первой вершины
     * @param x2 координата X второй вершины
     * @param y2 координата Y второй вершины
     * @param x3 координата X третьей вершины
     * @param y3 координата Y третьей вершины
     * @throws IllegalArgumentException если точки лежат на одной прямой
     */
    public Triangle(double x1, double y1, double x2, double y2, double x3, double y3) {
        double side1 = calculateDistance(x1, y1, x2, y2);
        double side2 = calculateDistance(x2, y2, x3, y3);
        double side3 = calculateDistance(x3, y3, x1, y1);

        if (!isValidTriangle(side1, side2, side3)) {
            throw new IllegalArgumentException("Точки не образуют треугольник. Возможно, они лежат на одной прямой.");
        }

        SetAll(side1, side2, side3);
    }

    /**
     * Вычисляет расстояние между двумя точками на плоскости.
     *
     * @param x1 координата X первой точки
     * @param y1 координата Y первой точки
     * @param x2 координата X второй точки
     * @param y2 координата Y второй точки
     * @return расстояние между точками
     */
    public static double calculateDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    /**
     * Проверяет, могут ли три отрезка образовать треугольник.
     * Используется неравенство треугольника: сумма любых двух сторон больше третьей.
     *
     * @param a длина первой стороны
     * @param b длина второй стороны
     * @param c длина третьей стороны
     * @return true если стороны могут образовать треугольник, иначе false
     */
    public static boolean isValidTriangle(double a, double b, double c) {
        return (a + b > c) && (a + c > b) && (b + c > a);
    }

    /**
     * Вычисляет периметр треугольника.
     *
     * @return периметр треугольника (сумма всех сторон)
     */
    public double getPerimeter() {
        return side1 + side2 + side3;
    }

    /**
     * Вычисляет площадь треугольника по формуле Герона.
     *
     * @return площадь треугольника
     */
    public double getArea() {
        double p = getPerimeter() / 2; // Полу периметр
        return Math.sqrt(p * (p - side1) * (p - side2) * (p - side3));
    }

    /**
     * Возвращает строковое представление треугольника.
     *
     * @return строка с информацией о длинах сторон треугольника
     */
    @Override // Переопределение наследуемого метода от Object
    public String toString() {
        return String.format("Треугольник со сторонами: %.2f, %.2f, %.2f", side1, side2, side3);
    }
}