// arrays.cpp
#include "arrays.hpp"
#include <iostream>
#include <fstream>
#include <cstdlib>
#include <stdexcept>

namespace file_work {
    /**
     * @brief Сохраняет массив в текстовый файл
     * @param arr Указатель на массив для сохранения
     * @param size Количество элементов в массиве
     * @param filename Имя файла для сохранения
     * @return true если сохранение успешно, false в случае ошибки
     * @note Каждый элемент сохраняется на отдельной строке
     */
    bool save_array_to_file(const int* arr, size_t size, const std::string& filename) {
        if (!arr) {
            throw std::invalid_argument("Указатель на массив равен nullptr.");
        }

        std::ofstream file(filename);
        if (!file.is_open()) {
            throw std::runtime_error("Не удалось открыть файл для записи: " + filename);
        }

        for (size_t i = 0; i < size; ++i) {
            file << arr[i] << '\n';
        }

        if (file.fail()) {
            throw std::runtime_error("Ошибка записи в файл: " + filename);
        }

        return true;
    }
    /**
     * @brief Загружает массив из текстового файла
     * @param filename Имя файла для загрузки
     * @param out_size[out] Количество загруженных элементов
     * @return Указатель на новый массив или nullptr при ошибке
     * @warning Вызывающая сторона отвечает за освобождение памяти с помощью delete[]
     * @note Файл должен содержать по одному целому числу на каждой строке
     */
    int* load_array_from_file(const std::string& filename, size_t& out_size) {
        std::ifstream file(filename);
        if (!file.is_open()) {
            throw std::runtime_error("Не удалось открыть файл для чтения: " + filename);
        }

        // Подсчёт количества чисел
        int temp;
        size_t count = 0;
        while (file >> temp) {
            ++count;
        }

        if (count == 0) {
            out_size = 0;
            return nullptr;
        }

        file.clear();
        file.seekg(0);

        int* arr = new int[count]; // может бросить исключение
        for (size_t i = 0; i < count; ++i) {
            if (!(file >> arr[i])) {
                delete[] arr;
                throw std::runtime_error("Ошибка чтения данных из файла: " + filename);
            }
        }

        out_size = count;
        return arr;
    }

} // namespace file_work

// В arrays.cpp
#include <iostream>
#include <limits>
#include <stdexcept>

namespace auxiliary {

int* input_array_from_keyboard(size_t count) {
    const size_t MAX_SIZE_KEYBOARD = 10000;
    if (count == 0) {
        throw std::invalid_argument("Количество чисел должно быть больше нуля.");
    }
    if (count > MAX_SIZE_KEYBOARD) {
        throw std::invalid_argument("Для ручного ввода максимальное количество — " +
                                    std::to_string(MAX_SIZE_KEYBOARD) + " чисел.");
    }

    int* arr = new int[count]; // может выбросить std::bad_alloc

    std::cout << "Введите " << count << " целых чисел:\n";
    for (size_t i = 0; i < count; ++i) {
        if (!(std::cin >> arr[i])) {
            delete[] arr;
            throw std::invalid_argument("Некорректный ввод числа " + std::to_string(i + 1) + ".");
        }
    }

    // Очистка остатка строки (если пользователь ввёл лишнее)
    std::cin.ignore(std::numeric_limits<std::streamsize>::max(), '\n');

    std::cout << "\nУспешно введено " << count << " чисел.\n";
    return arr;
}

} // namespace auxiliary