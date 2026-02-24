/**
 * @file main.cpp
 * @brief Главная программа: интерактивный интерфейс для вычисления суммы квадратов.
 *
 * Позволяет пользователю:
 * - либо сгенерировать случайный массив (числа от 1 до 10),
 * - либо загрузить массив из файла,
 * - либо ввести массив с клавиатуры.
 *
 * После вычисления выводит результат и предлагает сохранить массив.
 */

#include <iostream>
#include <vector>
#include <cstdlib>   // rand(), srand()
#include <ctime>     // time()
#include <cassert>   // assert
#include <string>    // std::string
#include <limits>    // numeric_limits
#include <stdexcept> // исключения
#include "module.hpp"

int main() {
    using namespace std;
    using namespace mathfunc;
    using namespace auxiliary;
    using namespace file_work;

    try {
        // Тесты (базовая проверка корректности)
        assert(sum_of_powers({}) == 0);
        assert(sum_of_powers({3}) == 9);
        assert(sum_of_powers({1, 2, 3}) == 14);   // 1² + 2² + 3² = 1 + 4 + 9 = 14
        assert(sum_of_powers({2, 2}) == 8);       // 4 + 4 = 8
        assert(sum_of_powers({1, 1, 1, 1}) == 4); // 1×4 = 4
    } catch (const exception& e) {
        cerr << "Ошибка в тестах: " << e.what() << endl;
        return 1;
    }

    // Интерактивный ввод режима работы
    cout << "Выберите режим работы:\n"
         << "1 - сгенерировать случайный массив (ввести длину)\n"
         << "2 - загрузить массив из файла\n"
         << "3 - ввести массив с клавиатуры\n"
         << "Ваш выбор (1, 2 или 3): ";

    int mode;
    if (!(cin >> mode) || (mode < 1 || mode > 3)) {
        cerr << "Ошибка: нужно ввести 1, 2 или 3.\n";
        return 1;
    }

    vector<int> nums;
    
    try {
        // Инициализация генератора случайных чисел
        std::srand(time(0));

        // Получение данных
        if (mode == 1) {
            // Генерация случайного массива
            const int MAX_SIZE = 1'000'000;
            int n;
            cout << "\nСколько чисел сгенерировать? (1–" << MAX_SIZE << "): ";
            if (!(cin >> n) || n <= 0 || n > MAX_SIZE) {
                throw runtime_error("Некорректное количество чисел.");
            }

            nums = random_vector(n);

        } else if (mode == 2) {
            // Загрузка из файла
            string filename;
            cout << "\nВведите имя файла для загрузки: ";
            cin >> filename;

            nums = load_vector_from_file(filename);
            if (nums.empty()) {
                throw runtime_error("Не удалось загрузить данные из файла (файл не найден, пуст или содержит ошибки).");
            }

            cout << "\nЗагружено " << nums.size() << " чисел из файла \"" << filename << "\".\n";
            
        } else if (mode == 3) {
            // Ввод с клавиатуры
            int n;
            cout << "Сколько чисел вы хотите ввести? (введите количество): ";
            if (!(cin >> n) || n <= 0) {
                throw runtime_error("Некорректное количество чисел.");
            }
            
            const int MAX_SIZE_KEYBOARD = 10000; // Ограничение для ручного ввода
            if (n > MAX_SIZE_KEYBOARD) {
                throw runtime_error("Для ручного ввода максимальное количество - " + 
                                    to_string(MAX_SIZE_KEYBOARD) + " чисел.");
            }
            
            cout << "Введите " << n << " целых чисел (через пробел или каждое с новой строки):\n";
            
            nums.resize(n);
            for (int i = 0; i < n; i++) {
                if (!(cin >> nums[i])) {
                    // Очистка потока ввода
                    cin.clear();
                    cin.ignore(numeric_limits<streamsize>::max(), '\n');
                    throw runtime_error("Некорректный ввод числа " + to_string(i + 1) + ".");
                }
            }
            
            cout << "\nУспешно введено " << n << " чисел.\n";
        }

        // Вычисление и вывод результата
        int result = sum_of_powers(nums);

        cout << "\nМассив:\n";
        print_vector_by_10(nums);
        cout << "Результат (сумма квадратов): " << result << "\n\n";

        // Сохранение (по желанию)
        string save_filename;
        cout << "Сохранить этот массив в файл? (введите имя файла или '-' чтобы пропустить): ";
        cin >> save_filename;

        if (save_filename != "-") {
            try {
                if (save_vector_to_file(nums, save_filename)) {
                    cout << "Массив успешно сохранён в файл \"" << save_filename << "\".\n";
                } else {
                    cerr << "Предупреждение: не удалось сохранить файл.\n";
                }
            } catch (const exception& e) {
                cerr << "Ошибка при сохранении файла: " << e.what() << endl;
            }
        }

    } catch (const exception& e) {
        cerr << "Ошибка: " << e.what() << endl;
        return 1;
    } catch (...) {
        cerr << "Неизвестная ошибка." << endl;
        return 1;
    }

    return 0;
}