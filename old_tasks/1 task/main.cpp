// Автор: Томских Т.К. (Ondosh)

#include <cassert>
#include <iostream>
#include <string>
#include "calc.hpp"

int main(int argc, char* argv[]) {
    using namespace std;
    using namespace calculating;
    using namespace constants;

    // Проверка на --help / -h
    if (argc == 2) {
        string arg = argv[1];
        if (arg == "--help" || arg == "-h") {
            cout << "Использование: \n"
                 << "main.exe -- интерактивный ввод x, y, z\n"
                 << "main.exe 1 2 3 -- вычисление с заданными числами\n"
                 << "main.exe --help или -h  -- показать эту справку\n";
            return 0;
        }
    }

    // Обработка аргументов командной строки
    float x, y, z;
    bool use_cli = false; // Булеан для проверки на использование интерфейса CLI.

    // Проверка на наличие 3 аргументов x, y, z
    if (argc != 4) {
        cerr << "Используйте: " << argv[0] << " x y z\n";
        return 1;
    }

    // Пробуем преобразовать строку в число с плавающей точкой
    try {
        x = stof(argv[1]);
        y = stof(argv[2]);
        z = stof(argv[3]);
        use_cli = true;
    }
    catch (const invalid_argument& e) {
        cerr << "Ошибка: один из аргументов не является числом.\n";
        return 1;
    }
    catch (const out_of_range& e) {
        cerr << "Ошибка: число слишком велико или мало.\n";
        return 1;
    }

    // Тесты
    assert(abs(calculate_b(1, 1, 1) - 1) < e);
    assert(abs(calculate_a(1, 2, 3) - 1.81963) < e);
    assert(abs(calculate_b(2, 2, 2) - 1) < e);

    // Получение значений: ввод с клавиатуры
    if (!use_cli) {
        // Интерактивный режим
        cin.exceptions(ios_base::failbit);
        try {
            cout << "Введите х: ";
            cin >> x;
            cout << "Введите у: ";
            cin >> y;
            cout << "Введите z: ";
            cin >> z;
        } catch (const ios_base::failure& err) {
            cout << "Ошибка ввода: " << err.what() << endl;
            cout << "Введены некорректные данные!" << endl;
            cin.clear();
            cin.ignore(numeric_limits<streamsize>::max(), '\n');
            return 1;
        }
    }

    // Вычисление и вывод
    float a = calculate_a(x, y, z);
    float b = calculate_b(x, y, z);

    cout << a << endl;
    cout << b << endl;

    return 0;
}