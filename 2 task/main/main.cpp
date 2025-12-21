// main.cpp
#include <iostream>
#include <cstdlib>
#include <ctime>
#include <cassert>
#include <string>
#include <limits>
#include <stdexcept>
#include "arrays.hpp"

int main() {
    using namespace std;
    using namespace mathfunc;
    using namespace auxiliary;
    using namespace file_work;

    // Тесты функции sum_of_powers
    {
        assert(sum_of_powers<int>(nullptr, 0) == 0);
        int a1[] = {3};
        assert(sum_of_powers(a1, 1) == 9);
        int a2[] = {1, 2, 3};
        assert(sum_of_powers(a2, 3) == 14);
        int a3[] = {2, 2};
        assert(sum_of_powers(a3, 2) == 8);
        int a4[] = {1, 1, 1, 1};
        assert(sum_of_powers(a4, 4) == 4);
    }

    int* nums = nullptr;
    size_t count = 0;
    string filename;
    int mode = 0;

    try {
        cout << "Выберите режим работы:\n"
             << "1 - сгенерировать случайный массив (ввести длину)\n"
             << "2 - загрузить массив из файла\n"
             << "3 - ввести массив с клавиатуры\n"
             << "Ваш выбор (1, 2 или 3): " << flush;

        if (!(cin >> mode) || (mode < 1 || mode > 3)) {
            throw invalid_argument("Нужно ввести 1, 2 или 3.");
        }

        if (mode == 1) {
            srand(static_cast<unsigned int>(time(nullptr)));

            const int MAX_SIZE = 1'000'000;
            int n;
            cout << "\nСколько чисел сгенерировать? (1–" << MAX_SIZE << "): " << flush;
            if (!(cin >> n) || n <= 0 || n > MAX_SIZE) {
                throw invalid_argument("Некорректное количество чисел.");
            }

            nums = random_array<int>(static_cast<size_t>(n), count);
            if (!nums) {
                // Этого не должно произойти при использовании обычного new
                throw runtime_error("Не удалось создать массив.");
            }

        } else if (mode == 2) {
            cout << "\nВведите имя файла для загрузки: " << flush;
            cin >> filename;

            nums = load_array_from_file(filename, count);
            cout << "\nЗагружено " << count << " чисел из файла \"" << filename << "\".\n";

        } else if (mode == 3) {
            std::cout << "\n=== Режим ввода с клавиатуры ===\n";

            int n;
            std::cout << "Сколько чисел вы хотите ввести? (введите количество): " << std::flush;
            if (!(std::cin >> n) || n <= 0) {
                throw std::invalid_argument("Некорректное количество чисел.");
            }

            count = static_cast<size_t>(n);
            nums = auxiliary::input_array_from_keyboard(count);

        }

        int result = sum_of_powers(nums, count);
        cout << "\nМассив:\n";
        print_array_by_10(nums, count);
        cout << "\nРезультат (сумма квадратов): " << result << "\n\n";

        if (mode == 1 || mode == 3) {
            string save_filename;
            cout << "Сохранить этот массив в файл? (введите имя файла или '-' чтобы пропустить): " << flush;
            cin >> save_filename;

            if (save_filename != "-") {
                save_array_to_file(nums, count, save_filename);
                cout << "Массив успешно сохранён в файл \"" << save_filename << "\".\n";
            }
        }

    } catch (const exception& e) {
        delete[] nums;
        cerr << "Ошибка: " << e.what() << "\n";
        return 1;
    } catch (...) {
        delete[] nums;
        cerr << "Неизвестная ошибка.\n";
        return 1;
    }

    delete[] nums;
    return 0;
}