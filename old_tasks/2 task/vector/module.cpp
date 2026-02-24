/**
 * @file module.cpp
 * @brief Реализация функций, объявленных в module.hpp.
 *
 * Содержит определения для трёх пространств имён:
 * - mathfunc
 * - auxiliary
 * - file_work
 */

#include <vector>
#include <cstdlib>   // rand(), srand()
#include <ctime>     // time()
#include <iostream>  // cout, cerr
#include <fstream>   // ofstream, ifstream
#include <string>    // std::string
#include <stdexcept> // исключения
#include "module.hpp"

namespace mathfunc {

int sum_of_powers(const std::vector<int>& numbers) {
    try {
        int result = 0;
        for (int num : numbers) {
            // Проверка на возможное переполнение (примерная проверка)
            if (num > 46340 || num < -46340) { // sqrt(INT_MAX) примерно 46340
                throw std::overflow_error("Возможное переполнение при вычислении квадрата числа: " + std::to_string(num));
            }
            result += num * num;  // Квадрат числа
        }
        return result;
    } catch (const std::overflow_error& e) {
        // Перебрасываем исключение дальше
        throw;
    } catch (...) {
        throw std::runtime_error("Неизвестная ошибка при вычислении суммы квадратов.");
    }
}

}

namespace auxiliary {

std::vector<int> random_vector(int n) {
    try {
        if (n <= 0) {
            throw std::invalid_argument("Количество элементов должно быть положительным.");
        }

        std::vector<int> nums;
        nums.reserve(n);  // Оптимизация: избегаем лишних реаллокаций

        for (int i = 0; i < n; ++i) {
            // Генерируем число от 1 до 10 включительно
            int random_num = std::rand() % 10 + 1;
            nums.push_back(random_num);
        }
        return nums;
    } catch (const std::bad_alloc& e) {
        throw std::runtime_error("Не удалось выделить память для массива из " + std::to_string(n) + " элементов.");
    } catch (const std::exception& e) {
        throw std::runtime_error("Ошибка при генерации случайного массива: " + std::string(e.what()));
    }
}

void print_vector_by_10(const std::vector<int>& vec) {
    try {
        if (vec.empty()) {
            std::cout << "(пустой массив)\n";
            return;
        }

        for (size_t i = 0; i < vec.size(); ++i) {
            std::cout << vec[i];
            // Добавляем пробел, если это не последний элемент в строке
            if ((i + 1) % 10 == 0) {
                std::cout << '\n';  // Перенос после 10-го, 20-го и т.д.
            } else if (i + 1 < vec.size()) {
                std::cout << " ";
            }
        }
        // Если последняя строка неполная — завершаем её переводом строки
        if (vec.size() % 10 != 0) {
            std::cout << '\n';
        }
        
        // Проверка состояния потока вывода
        if (std::cout.fail()) {
            throw std::runtime_error("Ошибка вывода в консоль.");
        }
    } catch (const std::exception& e) {
        std::cerr << "Ошибка при выводе массива: " << e.what() << std::endl;
        throw;
    }
}

} // namespace auxiliary

namespace file_work {

bool save_vector_to_file(const std::vector<int>& vec, const std::string& filename) {
    try {
        if (filename.empty()) {
            throw std::invalid_argument("Имя файла не может быть пустым.");
        }

        std::ofstream file(filename);
        if (!file.is_open()) {
            throw std::runtime_error("Не удалось открыть файл для записи: " + filename);
        }

        for (const int& num : vec) {
            file << num << '\n';
            if (file.fail()) {
                throw std::runtime_error("Ошибка записи данных в файл: " + filename);
            }
        }

        file.close();
        return true;
    } catch (const std::exception& e) {
        std::cerr << "Ошибка при сохранении в файл: " << e.what() << std::endl;
        return false;
    }
}

std::vector<int> load_vector_from_file(const std::string& filename) {
    std::vector<int> vec;
    
    try {
        if (filename.empty()) {
            throw std::invalid_argument("Имя файла не может быть пустым.");
        }

        std::ifstream file(filename);
        if (!file.is_open()) {
            throw std::runtime_error("Не удалось открыть файл для чтения: " + filename);
        }

        int num;
        while (file >> num) {
            vec.push_back(num);
            
            // Проверка на слишком большой файл
            if (vec.size() > 10'000'000) {
                throw std::runtime_error("Файл слишком большой. Максимальное количество чисел: 10,000,000");
            }
        }

        // Проверка состояния потока после чтения
        if (file.bad()) {
            throw std::runtime_error("Критическая ошибка ввода/вывода при чтении " + filename);
        } else if (file.fail() && !file.eof()) {
            // Например, встречена буква вместо числа
            std::cerr << "Предупреждение: некорректные данные в файле " << filename << " (чтение остановлено)" << std::endl;
        }

        file.close();
    } catch (const std::bad_alloc& e) {
        std::cerr << "Ошибка: недостаточно памяти для загрузки файла " << filename << std::endl;
        vec.clear();
    } catch (const std::exception& e) {
        std::cerr << "Ошибка при загрузке файла: " << e.what() << std::endl;
        vec.clear();
    }
    
    return vec;
}

} // namespace file_work