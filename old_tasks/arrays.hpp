// Author: Ondosh
// Модуль для работы с массивами целых чисел
// Пространства имён:
// - mathfunc: математические операции над массивами
// - auxiliary: утилиты для создания и отображения массивов  
// - file_work: операции ввода-вывода с файлами

#ifndef MODULE_HPP
#define MODULE_HPP

// СДЕЛАТЬ THROW ТАМ ГДЕ НУЖНО
#include <string>
#include <cstddef>  // size_t
#include <iostream>

namespace mathfunc {
    /**
     * @brief Вычисляет сумму квадратов элементов массива
     * @param arr Указатель на массив любого числового типа (допускается nullptr)
     * @param size Количество элементов в массиве
     * @return Сумма квадратов всех элементов массива или 0, если массив пуст или не существует
     */
    template<typename T>
    T sum_of_powers(const T* arr, size_t size) {
        if (!arr || size == 0) return T(0);
        
        T result = T(0);
        for (size_t i = 0; i < size; ++i) {
            result += arr[i] * arr[i];
        }
        return result;
    }
}


namespace auxiliary {
    /**
     * @brief Создаёт массив случайных чисел
     * @param n Требуемый размер массива
     * @param out_size[out] Фактически созданный размер массива
     * @return Указатель на новый массив или nullptr при ошибке выделения памяти
     * @note Диапазон случайных чисел: от 1 до 10 включительно
     * @warning Вызывающая сторона отвечает за освобождение памяти с помощью delete[]
     */
    template<typename T>
    T* random_array(size_t n, size_t& out_size) {
        if (n <= 0) {
            out_size = 0;
            return nullptr;
        }

        T* arr = new (std::nothrow) T[n];
        if (!arr) {
            out_size = 0;
            return nullptr;
        }

        for (size_t i = 0; i < n; ++i) {
            arr[i] = static_cast<T>(std::rand() % 10 + 1);
        }

        out_size = n;
        return arr;
    }

    /**
     * @brief Выводит массив на стандартный вывод с форматированием
     * @param arr Указатель на массив для вывода
     * @param size Количество элементов в массиве
     * @note Элементы выводятся по 10 в строке, разделённые пробелами
     */
    template<typename T>
    void print_array_by_10(const T* arr, size_t size) {
        if (!arr) return;
        for (size_t i = 0; i < size; ++i) {
            std::cout << arr[i] << " ";
            if ((i + 1) % 10 == 0) {
                std::cout << std::endl;
            }
        }
        if (size % 10 != 0) {
            std::cout << std::endl;
        }
    }
}

namespace file_work {
    /**
     * @brief Сохраняет массив в текстовый файл
     * @param arr Указатель на массив для сохранения
     * @param size Количество элементов в массиве
     * @param filename Имя файла для сохранения
     * @return true если сохранение успешно, false в случае ошибки
     * @note Каждый элемент сохраняется на отдельной строке
     */
    bool save_array_to_file(const int* arr, size_t size, const std::string& filename);

    /**
     * @brief Загружает массив из текстового файла
     * @param filename Имя файла для загрузки
     * @param out_size[out] Количество загруженных элементов
     * @return Указатель на новый массив или nullptr при ошибке
     * @warning Вызывающая сторона отвечает за освобождение памяти с помощью delete[]
     * @note Файл должен содержать по одному целому числу на каждой строке
     */
    int* load_array_from_file(const std::string& filename, size_t& out_size);
}

#endif // MODULE_HP