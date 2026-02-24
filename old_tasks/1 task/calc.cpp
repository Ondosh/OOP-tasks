// Автор: Томских Т.К. (Ondosh)

#include "calc.hpp"

namespace calculating {
    /**
    * @brief Функция для вычисления значения по заданной формуле.
    *
    * Формула находится по ссылке ниже:
    * https://ivtipm.github.io/Programming/Glava01/index01.htm#z11
    * 
    * @param x, y, z - переменные, задающиеся пользователем.
    * @return - возвращает результат вычисления по формуле.
    */
    float calculate_a(float x, float y, float z){
        float a;
        a = (3 + exp(y - 1)) / (1 + (pow(x, 2)*abs(y - tan(z))));
        return a;
    }

    /**
    * @brief Функция для вычисления значения по заданной формуле.
    *
    * Формула находится по ссылке ниже:
    * https://ivtipm.github.io/Programming/Glava01/index01.htm#z11
    * 
    * @param x, y, z - переменные, задающиеся пользователем.
    * @return - возвращает результат вычисления по формуле.
    */
    float calculate_b(float x, float y, float z){
        float b;
        b = (1 + abs(y - x)) + (pow(y - x, 2) / 2) + (pow(abs(y - x), 3) / 3);
        return b;
    }
}
