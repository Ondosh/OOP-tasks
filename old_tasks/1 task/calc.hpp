// // Автор: Томских Т.К. (Ondosh)
// ar rcs libmylib.a math.o utils.o

// # Или из всех .o файлов в папке
// ar rcs libmylib.a *.o
// ```

// **4. Компиляция основной программы с библиотекой**
// ```bash
// g++ main.cpp libmylib.a -o program.exe
#ifndef MODULE // Защита от повторного включения
#define MODULE
#include <cmath> // Исправлено: рекомендуется использовать <cmath> вместо <math.h> в C++

namespace calculating {
    /**
     * @brief Вычисляет значение величины `a` по формуле:
     *        a = (sqrt(|x - 1|) - pow(|y - 1|, 1.0/3.0)) / (1 + (x * x) / 2 + (y * y) / 4)
     * @param x, y, z — входные значения (вещественные числа)
     * @return Значение выражения `a` типа float
     */
    float calculate_a(float x, float y, float z);

    /**
     * @brief Вычисляет значение величины `b` по формуле:
     *        b = (1 + pow(z, 2) / 2 + pow(z, 4) / 4) / (1 + pow(z, 2))
     * @param x, y, z — входные значения (вещественные числа)
     * @return Значение выражения `b` типа float
     *
     * @note При z = 0 функция возвращает 1.0.
     */
    float calculate_b(float x, float y, float z);
}

namespace constants {
    /// @brief Малая величина для сравнения вещественных чисел с учётом погрешности
    constexpr float e = 0.0001f; 
}
#endif