#include "BankUser.hpp"
#include <iostream>
#include <fstream>
#include <vector>

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
    std::cout << "=== БАНКОВСКАЯ СИСТЕМА: ДЕМОНСТРАЦИЯ КЛАССА BankUser ===\n\n";
    
    try {
        // ==================== 1. СОЗДАНИЕ КЛИЕНТОВ ====================
        std::cout << "1. СОЗДАНИЕ КЛИЕНТОВ:\n";
        std::cout << "------------------------\n";
        
        BankUser client1("Иванов Иван Иванович", "CLIENT001", 5000.0);
        BankUser client2("Петрова Анна Сергеевна", "CLIENT002", 3000.0);
        BankUser client3("Сидоров Михаил Петрович", "CLIENT003", 10000.0);
        
        std::cout << "Создано 3 клиента:\n";
        std::cout << "  - " << client1.generate_report() << "\n";
        std::cout << "  - " << client2.generate_report() << "\n";
        std::cout << "  - " << client3.generate_report() << "\n\n";
        
        // ==================== 2. ОПЕРАЦИИ С БАЛАНСОМ ====================
        std::cout << "2. ОПЕРАЦИИ С БАЛАНСОМ:\n";
        std::cout << "------------------------\n";
        
        // Внесение средств
        std::cout << "Клиент " << client1.get_full_name() << " вносит 1500 руб.\n";
        client1.deposit(1500.0);
        std::cout << "  Новый баланс: " << client1.get_current_balance() << " руб.\n";
        
        // Снятие средств
        std::cout << "\nКлиент " << client2.get_full_name() << " снимает 800 руб.\n";
        client2.withdraw(800.0);
        std::cout << "  Новый баланс: " << client2.get_current_balance() << " руб.\n";
        
        // ==================== 3. ПЕРЕВОДЫ МЕЖДУ СЧЕТАМИ ====================
        std::cout << "\n3. ПЕРЕВОДЫ МЕЖДУ СЧЕТАМИ:\n";
        std::cout << "---------------------------\n";
        
        std::cout << "Перевод от " << client1.get_full_name() 
                  << " к " << client2.get_full_name() << " на сумму 2000 руб.\n";
        client1.transfer(2000.0, client2);
        
        std::cout << "\nРезультаты перевода:\n";
        std::cout << "  " << client1.generate_report() << "\n";
        std::cout << "  " << client2.generate_report() << "\n";
        
        // ==================== 4. ОБНОВЛЕНИЕ ДАННЫХ ====================
        std::cout << "\n4. ОБНОВЛЕНИЕ ДАННЫХ КЛИЕНТА:\n";
        std::cout << "-----------------------------\n";
        
        std::cout << "Клиент " << client3.get_full_name() << " сменил фамилию.\n";
        client3.update_name("Кузнецов Михаил Петрович");
        std::cout << "  Новое имя: " << client3.get_full_name() << "\n";
        std::cout << "  Полный отчет: " << client3.generate_report() << "\n";
        
        // ==================== 5. ЭКСПОРТ В ФАЙЛ ====================
        std::cout << "\n5. ЭКСПОРТ ДАННЫХ В ФАЙЛ:\n";
        std::cout << "-------------------------\n";

        const std::string filename = "bank_clients.txt";

        // Сначала очищаем файл (false = не добавлять, а заменять)
        std::cout << "Создание/очистка файла " << filename << "...\n";
        std::ofstream clear_file(filename, std::ios::trunc);
        clear_file.close();

        // Затем добавляем данные клиентов
        std::cout << "Экспорт данных клиентов...\n";
        client1.export_to_file(filename);  // по умолчанию append = true
        client2.export_to_file(filename);
        client3.export_to_file(filename);

        std::cout << "Данные экспортированы в файл: " << filename << "\n";

        // Показать содержимое файла
        std::cout << "\nСодержимое файла " << filename << ":\n";
        std::cout << "------------------------------------\n";
        std::ifstream check_file(filename);
        std::string line;
        int line_num = 1;
        while (std::getline(check_file, line)) {
            std::cout << "  Строка " << line_num++ << ": " << line << "\n";
        }
        check_file.close();

        // ==================== 6. ИМПОРТ ИЗ ФАЙЛА ====================
        std::cout << "\n6. ИМПОРТ ДАННЫХ ИЗ ФАЙЛА:\n";
        std::cout << "---------------------------\n";

        // Создадим файл для импорта (используем подчеркивания вместо пробелов)
        std::ofstream test_file("test_import.txt", std::ios::trunc); // заменяем файл
        test_file << "Смирнова_Ольга_Игоревна CLIENT004 7500.5\n";
        test_file << "Васильев_Дмитрий_Александрович CLIENT005 12000.75\n";
        test_file.close();

        std::cout << "Создан тестовый файл test_import.txt\n";

        // Импорт данных
        BankUser imported_client("", "", 0.0);
        std::ifstream import_stream("test_import.txt");

        if (!import_stream.is_open()) {
            std::cout << "  Ошибка: не удалось открыть файл test_import.txt\n";
        } else {
            std::cout << "Импортируем первого клиента из файла:\n";
            try {
                imported_client.import_from_stream(import_stream);
                std::cout << "  Импортировано: " << imported_client.generate_report() << "\n";
                
                // Попробуем импортировать второго клиента из того же файла
                BankUser imported_client2("", "", 0.0);
                std::ifstream import_stream2("test_import.txt");
                // Пропускаем первую строку
                std::string temp;
                std::getline(import_stream2, temp);
                imported_client2.import_from_stream(import_stream2);
                import_stream2.close();
                
                std::cout << "Импортируем второго клиента из файла:\n";
                std::cout << "  Импортировано: " << imported_client2.generate_report() << "\n";
                
            } catch (const std::exception& e) {
                std::cout << "  Ошибка импорта: " << e.what() << "\n";
            }
            import_stream.close();
        }
        // ==================== 7. ОБРАБОТКА ОШИБОК ====================
        std::cout << "\n7. ДЕМОНСТРАЦИЯ ОБРАБОТКИ ОШИБОК:\n";
        std::cout << "---------------------------------\n";
        
        try {
            std::cout << "Попытка создать клиента с отрицательным депозитом:\n";
            BankUser bad_client("Ошибочный Клиент", "ERROR001", -100.0);
        } catch (const std::invalid_argument& e) {
            std::cout << "  Поймано исключение: " << e.what() << "\n";
        }
        
        try {
            std::cout << "\nПопытка снять больше средств, чем есть на счете:\n";
            client2.withdraw(100000.0);
        } catch (const std::runtime_error& e) {
            std::cout << "  Поймано исключение: " << e.what() << "\n";
        }
        
        try {
            std::cout << "\nПопытка перевести средства самому себе:\n";
            client1.transfer(100.0, client1);
        } catch (const std::invalid_argument& e) {
            std::cout << "  Поймано исключение: " << e.what() << "\n";
        }
        
        // ==================== 8. ФИНАЛЬНЫЙ ОТЧЕТ ====================
        std::cout << "\n8. ФИНАЛЬНЫЙ ОТЧЕТ ПО ВСЕМ КЛИЕНТАМ:\n";
        std::cout << "------------------------------------\n";
        
        std::vector<BankUser*> all_clients = {&client1, &client2, &client3, &imported_client};
        
        double total_balance = 0.0;
        for (const auto& client : all_clients) {
            std::cout << "  " << client->generate_report() << "\n";
            total_balance += client->get_current_balance();
        }
        
        std::cout << "\nОбщая сумма всех счетов: " << total_balance << " руб.\n";
        std::cout << "Количество клиентов: " << all_clients.size() << "\n";
        
    } catch (const std::exception& e) {
        std::cerr << "\n!!! КРИТИЧЕСКАЯ ОШИБКА: " << e.what() << "\n";
        return 1;
    }
    
    // ==================== 9. ЧТЕНИЕ ЭКСПОРТИРОВАННОГО ФАЙЛА ====================
    std::cout << "\n9. СОДЕРЖИМОЕ ЭКСПОРТИРОВАННОГО ФАЙЛА:\n";
    std::cout << "--------------------------------------\n";
    
    std::ifstream exported_file("bank_clients.txt");
    if (exported_file.is_open()) {
        std::string line;
        int line_num = 1;
        while (std::getline(exported_file, line)) {
            std::cout << "  Строка " << line_num++ << ": " << line << "\n";
        }
        exported_file.close();
    } else {
        std::cout << "  Файл не найден или не может быть открыт\n";
    }
    
    std::cout << "\n=== ПРОГРАММА УСПЕШНО ЗАВЕРШЕНА ===\n";
    return 0;
}