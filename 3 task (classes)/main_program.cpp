#include "BankUser.hpp"
#include <iostream>
#include <fstream>
#include <vector>
using namespace std;

/**
 * @brief Демонстрационная программа использования класса BankUser
 * 
 * Программа демонстрирует все основные возможности класса BankUser:
 * 1. Создание клиентов
 * 2. Операции со счетами
 * 3. Работу с файлами
 * 4. Обработку исключений
 */
int main() {
    cout << "Банковская система: демонстрация класса BankUser\n\n";
    
    try {
        // 1. Создание клиентов
        cout << "1. Создание клиентов:\n";
        
        BankUser client1("Иванов Иван Иванович", "CLIENT001", 5000.0);
        BankUser client2("Петрова Анна Сергеевна", "CLIENT002", 3000.0);
        BankUser client3("Сидоров Михаил Петрович", "CLIENT003", 10000.0);
        
        cout << "Создано 3 клиента:\n";
        cout << "  - " << client1.generate_report() << "\n";
        cout << "  - " << client2.generate_report() << "\n";
        cout << "  - " << client3.generate_report() << "\n\n";
        
        // 2. Операции с балансом
        cout << "2. Операции с балансом:\n";
        
        cout << "Клиент " << client1.get_full_name() << " вносит 1500 руб.\n";
        client1.deposit(1500.0);
        cout << "  Новый баланс: " << client1.get_current_balance() << " руб.\n";
        
        cout << "\nКлиент " << client2.get_full_name() << " снимает 800 руб.\n";
        client2.withdraw(800.0);
        cout << "  Новый баланс: " << client2.get_current_balance() << " руб.\n\n";
        
        // 3. Переводы между счетами
        cout << "3. Переводы между счетами:\n";
        
        cout << "Перевод от " << client1.get_full_name() 
             << " к " << client2.get_full_name() << " на сумму 2000 руб.\n";
        client1.transfer(2000.0, client2);
        
        cout << "\nРезультаты перевода:\n";
        cout << "  " << client1.generate_report() << "\n";
        cout << "  " << client2.generate_report() << "\n\n";
        
        // 4. Обновление данных
        cout << "4. Обновление данных клиента:\n";
        
        cout << "Клиент " << client3.get_full_name() << " сменил фамилию.\n";
        client3.update_name("Кузнецов Михаил Петрович");
        cout << "  Новое имя: " << client3.get_full_name() << "\n";
        cout << "  Полный отчет: " << client3.generate_report() << "\n\n";
        
        // 5. Экспорт в файл
        cout << "5. Экспорт данных в файл:\n";

        string filename = "bank_clients.txt";

        // Очищаем файл
        cout << "Создание/очистка файла " << filename << "...\n";
        ofstream clear_file(filename, ios::trunc);
        clear_file.close();

        // Добавляем данные клиентов
        cout << "Экспорт данных клиентов...\n";
        client1.export_to_file(filename);
        client2.export_to_file(filename);
        client3.export_to_file(filename);

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
                cout << "  Импортировано: " << imported_client.generate_report() << "\n";
                
                // Импортируем второго клиента
                BankUser imported_client2("", "", 0.0);
                ifstream import_stream2("test_import.txt");
                string temp;
                getline(import_stream2, temp); // пропускаем первую строку
                imported_client2.import_from_stream(import_stream2);
                import_stream2.close();
                
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
        } catch (const invalid_argument& e) {
            cout << "  Поймано исключение: " << e.what() << "\n";
        }
        
        try {
            cout << "\nПопытка снять больше средств, чем есть на счете:\n";
            client2.withdraw(100000.0);
        } catch (const runtime_error& e) {
            cout << "  Поймано исключение: " << e.what() << "\n";
        }
        
        try {
            cout << "\nПопытка перевести средства самому себе:\n";
            client1.transfer(100.0, client1);
        } catch (const invalid_argument& e) {
            cout << "  Поймано исключение: " << e.what() << "\n";
        }
        cout << "\n";
        
        // 8. Финальный отчёт
        cout << "8. Финальный отчёт по всем клиентам:\n";
        
        vector<BankUser*> all_clients = {&client1, &client2, &client3, &imported_client};
        
        double total_balance = 0.0;
        for (const auto& client : all_clients) {
            cout << "  " << client->generate_report() << "\n";
            total_balance += client->get_current_balance();
        }
        
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