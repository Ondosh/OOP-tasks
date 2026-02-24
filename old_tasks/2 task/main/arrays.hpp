// arrays.hpp
#ifndef ARRAYS_HPP
#define ARRAYS_HPP

#include <string>
#include <cstddef>  // size_t
#include <iostream>

namespace mathfunc {
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
        if (n == 0) {
            out_size = 0;
            return nullptr;
        }

        T* arr = new T[n]; // throw при неудаче (стандартное поведение new)
        out_size = n;
        for (size_t i = 0; i < n; ++i) {
            arr[i] = static_cast<T>(std::rand() % 10 + 1);
        }
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

    /**
     * @brief Вводит массив целых чисел с клавиатуры
     * @param count Количество чисел для ввода
     * @return Указатель на новый массив или бросает исключение при ошибке
     * @warning Вызывающая сторона отвечает за освобождение памяти с помощью delete[]
     * @note Максимальный допустимый размер — 10000
     */
    int* input_array_from_keyboard(size_t count);
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

#endif // ARRAYS_HPP