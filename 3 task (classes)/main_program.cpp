#include "BankUser.hpp"
#include <iostream>
#include <fstream>
#include <sstream>
#include <cassert>
#include <stdexcept>
using namespace std;

/**
 * @brief Unit-тесты для класса BankUser с использованием assert
 */
void run_bankuser_tests() {
    cout << "Запуск unit-тестов для BankUser...\n";

    // Тест 1: корректная инициализация
    {
        BankUser u("Тест Тестов", "CLIENT001", 1000.0);
        assert(u.get_full_name() == "Тест Тестов");
        assert(u.get_client_ID() == "CLIENT001");
        assert(u.get_current_balance() == 1000.0);
    }

    // Тест 2: депозит
    {
        BankUser u("Депозит Тест", "CLIENT002", 500.0);
        u.deposit(200.0);
        assert(u.get_current_balance() == 700.0);
    }

    // Тест 3: снятие средств
    {
        BankUser u("Снятие Тест", "CLIENT003", 1000.0);
        u.withdraw(300.0);
        assert(u.get_current_balance() == 700.0);
    }

    // Тест 4: перевод между счетами
    {
        BankUser sender("Отправитель", "CLIENT004", 2000.0);
        BankUser receiver("Получатель", "CLIENT005", 500.0);
        sender.transfer(400.0, receiver);
        assert(sender.get_current_balance() == 1600.0);
        assert(receiver.get_current_balance() == 900.0);
    }

    // Тест 5: попытка перевода самому себе → исключение
    {
        BankUser u("Сам", "CLIENT006", 1000.0);
        bool caught = false;
        try {
            u.transfer(100.0, u);
        } catch (const invalid_argument& e) {
            caught = true;
        }
        assert(caught);
    }

    // Тест 6: попытка снять больше, чем есть → исключение
    {
        BankUser u("Недостаток", "CLIENT007", 100.0);
        bool caught = false;
        try {
            u.withdraw(200.0);
        } catch (const runtime_error& e) {
            caught = true;
        }
        assert(caught);
    }

    // Тест 7: отрицательный депозит → исключение
    {
        bool caught = false;
        try {
            BankUser u("Ошибка", "CLIENT008", -50.0);
        } catch (const invalid_argument& e) {
            caught = true;
        }
        assert(caught);
    }

    // Тест 8: экспорт и импорт 
    {
        const string test_file = "test_export_import.txt";
        {
            BankUser orig("Экспорт Тест", "CLIENT009", 1234.56);
            orig.export_to_file(test_file, false); // перезапись
        }

        BankUser loaded("", "", 0.0);
        ifstream f(test_file);
        assert(f.is_open());
        loaded.import_from_stream(f);
        f.close();

        assert(loaded.get_full_name() == "Экспорт Тест");
        assert(loaded.get_client_ID() == "CLIENT009");
        assert(loaded.get_current_balance() == 1234.56);
    }

    cout << "Все тесты пройдены успешно.\n\n";
}

/**
 * @brief Демонстрация работы с классом BankUser согласно ТЗ
 */
int main() {
    cout << "Банковская система: демонстрация класса BankUser (согласно ТЗ)\n\n";

    // Запуск автоматических тестов
    run_bankuser_tests();

    try {
        // 1. Статические объекты
        cout << "1. Статические объекты:\n";
        BankUser static_client1("Иванов Иван Иванович", "CLIENT001", 5000.0);
        BankUser static_client2("Петрова Анна Сергеевна", "CLIENT002", 3000.0);
        cout << "  " << static_client1.generate_report() << "\n";
        cout << "  " << static_client2.generate_report() << "\n\n";

        // 2. Динамическое создание объекта
        cout << "2. Динамическое создание объекта:\n";
        BankUser* dynamic_client = new BankUser("Сидоров Михаил Петрович", "CLIENT003", 10000.0);
        cout << "  " << dynamic_client->generate_report() << "\n";
        dynamic_client->deposit(500.0);
        cout << "  После депозита: " << dynamic_client->generate_report() << "\n\n";

        // 3. Массив из объектов (на стеке)
        cout << "3. Массив из объектов:\n";
        const int N = 2;
        BankUser stack_array[N] = {
            BankUser("Козлов Алексей", "CLIENT004", 7000.0),
            BankUser("Морозова Елена", "CLIENT005", 2000.0)
        };
        for (int i = 0; i < N; ++i) {
            cout << "  " << stack_array[i].generate_report() << "\n";
        }
        cout << "\n";

        // 4. Динамический массив из объектов
        cout << "4. Динамический массив из объектов:\n";
        BankUser* dynamic_array = new BankUser[N]{
            BankUser("Фёдоров Сергей", "CLIENT006", 4000.0),
            BankUser("Новикова Дарья", "CLIENT007", 9000.0)
        };
        for (int i = 0; i < N; ++i) {
            dynamic_array[i].withdraw(500.0);
            cout << "  " << dynamic_array[i].generate_report() << "\n";
        }
        cout << "\n";

        // 5. Массив из указателей на объекты
        cout << "5. Массив из указателей на объекты:\n";
        BankUser* ptr_array[N];
        ptr_array[0] = new BankUser("Григорьев Олег", "CLIENT008", 6000.0);
        ptr_array[1] = new BankUser("Жукова Ирина", "CLIENT009", 1000.0);

        for (int i = 0; i < N; ++i) {
            ptr_array[i]->transfer(300.0, *dynamic_client);
            cout << "  " << ptr_array[i]->generate_report() << "\n";
        }
        cout << "  После перевода получатель: " << dynamic_client->generate_report() << "\n\n";

        // 6. Экспорт в файл
        cout << "6. Экспорт в файл:\n";
        string filename = "bank_export.txt";
        ofstream clear_file(filename, ios::trunc);
        clear_file.close();

        static_client1.export_to_file(filename, true);
        static_client2.export_to_file(filename, true);
        dynamic_client->export_to_file(filename, true);
        stack_array[0].export_to_file(filename, true);
        stack_array[1].export_to_file(filename, true);
        dynamic_array[0].export_to_file(filename, true);
        dynamic_array[1].export_to_file(filename, true);
        ptr_array[0]->export_to_file(filename, true);
        ptr_array[1]->export_to_file(filename, true);

        cout << "  Данные экспортированы в " << filename << "\n\n";

        // Показ содержимого файла
        cout << "Содержимое файла " << filename << ":\n";
        ifstream check_file(filename);
        string line;
        while (getline(check_file, line)) {
            cout << "  " << line << "\n";
        }
        check_file.close();
        cout << "\n";

        // 7. Импорт из файла
        cout << "7. Импорт из файла:\n";
        BankUser imported("", "", 0.0);
        ifstream import_stream(filename);
        if (import_stream.is_open()) {
            try {
                imported.import_from_stream(import_stream);
                cout << "  Импортирован первый клиент: " << imported.generate_report() << "\n";
            } catch (const exception& e) {
                cout << "  Ошибка импорта: " << e.what() << "\n";
            }
            import_stream.close();
        }
        cout << "\n";

        // 8. Освобождение памяти
        cout << "8. Освобождение памяти:\n";
        delete dynamic_client;
        delete[] dynamic_array;
        for (int i = 0; i < N; ++i) {
            delete ptr_array[i];
        }
        cout << "  Память освобождена.\n\n";

    } catch (const exception& e) {
        cerr << "Критическая ошибка: " << e.what() << "\n";
        return 1;
    }

    cout << "Программа успешно завершена.\n";
    return 0;
}