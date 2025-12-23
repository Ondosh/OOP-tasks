#include "BankUser.hpp"
#include <iostream>
#include <fstream>
#include <vector>
#include <cassert>
using namespace std;

/**
 * @brief Демонстрационная программа использования класса BankUser
 * 
 * Программа демонстрирует все основные возможности класса BankUser:
 * 1. Создание клиентов
 * 2. Операции со счетами
 * 3. Работу с файлами
 * 4. Обработку исключений
 * 5. Автоматизированные проверки через assert
 */
int main() {
    cout << "Банковская система: демонстрация класса BankUser\n\n";
    
    try {
        // 1. Создание клиентов
        cout << "1. Создание клиентов:\n";
        
        vector<BankUser> clients = {
            BankUser("Иванов Иван Иванович", "CLIENT001", 5000.0),
            BankUser("Петрова Анна Сергеевна", "CLIENT002", 3000.0),
            BankUser("Сидоров Михаил Петрович", "CLIENT003", 10000.0)
        };
        
        // Тесты на инициализацию
        assert(clients[0].get_current_balance() == 5000.0);
        assert(clients[1].get_current_balance() == 3000.0);
        assert(clients[2].get_current_balance() == 10000.0);
        assert(clients[0].get_full_name() == "Иванов Иван Иванович");
        assert(clients[1].get_client_ID() == "CLIENT002");
        
        cout << "Создано " << clients.size() << " клиента(ов):\n";
        for (const auto& client : clients) {
            cout << "  - " << client.generate_report() << "\n";
        }
        cout << "\n";
        
        // 2. Операции с балансом
        cout << "2. Операции с балансом:\n";
        
        cout << "Клиент " << clients[0].get_full_name() << " вносит 1500 руб.\n";
        clients[0].deposit(1500.0);
        assert(clients[0].get_current_balance() == 6500.0); // 5000 + 1500
        cout << "  Новый баланс: " << clients[0].get_current_balance() << " руб.\n";
        
        cout << "\nКлиент " << clients[1].get_full_name() << " снимает 800 руб.\n";
        clients[1].withdraw(800.0);
        assert(clients[1].get_current_balance() == 2200.0); // 3000 - 800
        cout << "  Новый баланс: " << clients[1].get_current_balance() << " руб.\n\n";
        
        // 3. Переводы между счетами
        cout << "3. Переводы между счетами:\n";
        
        cout << "Перевод от " << clients[0].get_full_name() 
             << " к " << clients[1].get_full_name() << " на сумму 2000 руб.\n";
        clients[0].transfer(2000.0, clients[1]);
        
        // Проверка после перевода
        assert(clients[0].get_current_balance() == 4500.0); // 6500 - 2000
        assert(clients[1].get_current_balance() == 4200.0); // 2200 + 2000
        
        cout << "\nРезультаты перевода:\n";
        cout << "  " << clients[0].generate_report() << "\n";
        cout << "  " << clients[1].generate_report() << "\n\n";
        
        // 4. Обновление данных
        cout << "4. Обновление данных клиента:\n";
        
        cout << "Клиент " << clients[2].get_full_name() << " сменил фамилию.\n";
        clients[2].update_name("Кузнецов Михаил Петрович");
        assert(clients[2].get_full_name() == "Кузнецов Михаил Петрович");
        cout << "  Новое имя: " << clients[2].get_full_name() << "\n";
        cout << "  Полный отчёт: " << clients[2].generate_report() << "\n\n";
        
        // 5. Экспорт в файл
        cout << "5. Экспорт данных в файл:\n";

        string filename = "bank_clients.txt";

        // Очищаем файл
        cout << "Создание/очистка файла " << filename << "...\n";
        ofstream clear_file(filename, ios::trunc);
        clear_file.close();

        // Добавляем данные клиентов
        cout << "Экспорт данных клиентов...\n";
        for (auto& client : clients) {
            client.export_to_file(filename);
        }

        cout << "Данные экспортированы в файл: " << filename << "\n";

        // Показать содержимое файла
        cout << "\nСодержимое файла " << filename << ":\n";
        ifstream check_file(filename);
        string line;
        int line_num = 1;
        while (getline(check_file, line)) {
            cout << "  Строка " << line_num++ << ": " << line << "\n";
        }
        check_file.close();
        cout << "\n";
        
        // 6. Импорт из файла
        cout << "6. Импорт данных из файла:\n";

        // Создаём тестовый файл
        ofstream test_file("test_import.txt", ios::trunc);
        test_file << "Смирнова_Ольга_Игоревна CLIENT004 7500.5\n";
        test_file << "Васильев_Дмитрий_Александрович CLIENT005 12000.75\n";
        test_file.close();

        cout << "Создан тестовый файл test_import.txt\n";

        BankUser imported_client("", "", 0.0);
        ifstream import_stream("test_import.txt");

        if (!import_stream.is_open()) {
            cout << "  Ошибка: не удалось открыть файл test_import.txt\n";
        } else {
            cout << "Импортируем первого клиента из файла:\n";
            try {
                imported_client.import_from_stream(import_stream);
                assert(imported_client.get_full_name() == "Смирнова_Ольга_Игоревна");
                assert(imported_client.get_client_ID() == "CLIENT004");
                assert(abs(imported_client.get_current_balance() - 7500.5) < 1e-5);
                cout << "  Импортировано: " << imported_client.generate_report() << "\n";
                
                // Импортируем второго клиента
                BankUser imported_client2("", "", 0.0);
                ifstream import_stream2("test_import.txt");
                string temp;
                getline(import_stream2, temp); // пропускаем первую строку
                imported_client2.import_from_stream(import_stream2);
                import_stream2.close();
                
                assert(imported_client2.get_full_name() == "Васильев_Дмитрий_Александрович");
                assert(abs(imported_client2.get_current_balance() - 12000.75) < 1e-5);
                
                cout << "Импортируем второго клиента из файла:\n";
                cout << "  Импортировано: " << imported_client2.generate_report() << "\n";
                
            } catch (const exception& e) {
                cout << "  Ошибка импорта: " << e.what() << "\n";
            }
            import_stream.close();
        }
        cout << "\n";
        
        // 7. Обработка ошибок
        cout << "7. Демонстрация обработки ошибок:\n";
        
        try {
            cout << "Попытка создать клиента с отрицательным депозитом:\n";
            BankUser bad_client("Ошибочный Клиент", "ERROR001", -100.0);
            assert(false); // Не должно дойти до этой строки
        } catch (const invalid_argument& e) {
            cout << "  Поймано исключение: " << e.what() << "\n";
        }
        
        try {
            cout << "\nПопытка снять больше средств, чем есть на счете:\n";
            clients[1].withdraw(100000.0);
            assert(false); // Не должно выполниться
        } catch (const runtime_error& e) {
            cout << "  Поймано исключение: " << e.what() << "\n";
        }
        
        try {
            cout << "\nПопытка перевести средства самому себе:\n";
            clients[0].transfer(100.0, clients[0]);
            assert(false); // Самоперевод запрещён
        } catch (const invalid_argument& e) {
            cout << "  Поймано исключение: " << e.what() << "\n";
        }
        cout << "\n";
        
        // 8. Финальный отчёт
        cout << "8. Финальный отчёт по всем клиентам:\n";
        
        vector<const BankUser*> all_clients;
        for (const auto& client : clients) {
            all_clients.push_back(&client);
        }
        all_clients.push_back(&imported_client);
        
        double total_balance = 0.0;
        for (const auto* client : all_clients) {
            cout << "  " << client->generate_report() << "\n";
            total_balance += client->get_current_balance();
        }
        
        // Проверка итоговой суммы
        double expected_total = 4500.0 + 4200.0 + 10000.0 + 7500.5;
        assert(abs(total_balance - expected_total) < 1e-5);
        
        cout << "\nОбщая сумма всех счетов: " << total_balance << " руб.\n";
        cout << "Количество клиентов: " << all_clients.size() << "\n\n";
        
    } catch (const exception& e) {
        cerr << "\nКритическая ошибка: " << e.what() << "\n";
        return 1;
    }
    
    // 9. Чтение экспортированного файла
    cout << "9. Содержимое экспортированного файла:\n";
    
    ifstream exported_file("bank_clients.txt");
    if (exported_file.is_open()) {
        string line;
        int line_num = 1;
        while (getline(exported_file, line)) {
            cout << "  Строка " << line_num++ << ": " << line << "\n";
        }
        exported_file.close();
    } else {
        cout << "  Файл не найден или не может быть открыт\n";
    }
    
    cout << "\nПрограмма успешно завершена\n";
    return 0;
}