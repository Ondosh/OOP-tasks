#ifndef BANKUSER_HPP
#define BANKUSER_HPP

#include <string>
#include <fstream>

/**
 * @class BankUser
 * @brief Абстрактный тип данных "Клиент банка"
 * @warning (важно: ID должен начинаться с CLIENT!)
 * Класс представляет клиента банка с уникальным идентификатором
 * и основными операциями управления счетом.
 */
class BankUser {
private:
    std::string full_name;       ///< Полное имя клиента
    std::string client_ID;       ///< Уникальный идентификатор клиента
    double current_balance;      ///< Текущая сумма на счете

    void validate_non_negative_sum(double sum, const std::string& operation) const;
    void validate_positive_sum(double sum, const std::string& operation) const;
    void validate_sufficient_funds(double sum) const;

public:
    /**
     * @brief Конструктор клиента банка
     * @param client_name Полное имя клиента
     * @param client_id Уникальный идентификатор клиента
     * @param opening_deposit Начальный депозит (должен быть >= 0)
     * @throw std::invalid_argument Если opening_deposit < 0
     */
    BankUser(const std::string& client_name, const std::string& client_id, double opening_deposit);
    
    /**
     * @brief Получает текущий баланс клиента
     * @return Текущий баланс счета
     * @note Состояние объекта не изменяется
     */
    double get_current_balance() const;
    
    /**
     * @brief Получает полное имя клиента
     * @return Полное имя клиента
     * @note Состояние объекта не изменяется
     */
    std::string get_full_name() const;
    
    /**
     * @brief Получает идентификатор клиента
     * @return Уникальный идентификатор клиента
     * @note Состояние объекта не изменяется
     */
    std::string get_client_ID() const;
    
    /**
     * @brief Вносит средства на счет
     * @param sum Сумма для внесения (должна быть > 0)
     * @throw std::invalid_argument Если sum <= 0
     * @post Баланс увеличен на sum
     */
    void deposit(double sum);
    
    /**
     * @brief Снимает средства со счета
     * @param sum Сумма для снятия (должна быть > 0)
     * @throw std::invalid_argument Если sum <= 0
     * @throw std::runtime_error Если на счете недостаточно средств
     * @post Баланс уменьшен на sum
     */
    void withdraw(double sum);
    
    /**
     * @brief Переводит средства на другой счет
     * @param sum Сумма перевода (должна быть > 0)
     * @param recipient_account Ссылка на счет получателя
     * @throw std::invalid_argument Если sum <= 0 или получатель совпадает с отправителем
     * @throw std::runtime_error Если на счете недостаточно средств
     * @post Баланс текущего клиента уменьшен на sum, баланс получателя увеличен на sum
     */
    void transfer(double sum, BankUser& recipient_account);
    
    /**
     * @brief Обновляет имя клиента
     * @param updated_name Новое имя клиента
     * @post full_name заменен на updated_name
     */
    void update_name(const std::string& updated_name);
        
    /**
     * @brief Экспортирует данные клиента в файл
     * @param path Путь к целевому файлу
     * @param append Если true - добавляет в конец файла, если false - заменяет файл
     * @throw std::ios_base::failure Если операция открытия/записи в файл не удалась
     * @post Данные клиента записаны в файл в формате: имя id баланс
     */
    void export_to_file(const std::string& path, bool append = true) const;
    
    /**
     * @brief Импортирует данные клиента из потока
     * @param input_stream Открытый поток для чтения
     * @throw std::ios_base::failure Если операция чтения из потока невозможна
     * @post Атрибуты объекта заменены считанными значениями
     * @note Ожидает данные в формате: имя id баланс
     */
    void import_from_stream(std::ifstream& input_stream);
    
    /**
     * @brief Формирует текстовый отчет о состоянии клиента
     * @return Строка формата "Клиент: ..., ID: ..., Баланс: ..."
     * @note Состояние объекта не изменяется
     */
    std::string generate_report() const;
};

#endif // BANKUSER_HPP