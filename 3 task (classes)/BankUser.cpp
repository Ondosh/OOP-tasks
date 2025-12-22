#include "BankUser.hpp"
#include <stdexcept>
#include <sstream>

BankUser::BankUser(const std::string& client_name, const std::string& client_id, double opening_deposit) {
    validate_non_negative_sum(opening_deposit, "opening deposit");
    full_name = client_name;
    client_ID = client_id;
    current_balance = opening_deposit;
}

double BankUser::get_current_balance() const {
    return current_balance;
}

std::string BankUser::get_full_name() const {
    return full_name;
}

std::string BankUser::get_client_ID() const {
    return client_ID;
}

void BankUser::deposit(double sum) {
    validate_positive_sum(sum, "deposit");
    current_balance += sum;
}

void BankUser::withdraw(double sum) {
    validate_positive_sum(sum, "withdraw");
    validate_sufficient_funds(sum);
    current_balance -= sum;
}

void BankUser::transfer(double sum, BankUser& recipient_account) {
    validate_positive_sum(sum, "transfer");
    validate_sufficient_funds(sum);
    
    if (&recipient_account == this) {
        throw std::invalid_argument("Cannot transfer to the same account");
    }
    //
    
    current_balance -= sum;
    recipient_account.current_balance += sum;
}

void BankUser::update_name(const std::string& updated_name) {
    full_name = updated_name;
}

void BankUser::export_to_file(const std::string& path, bool append) const {
    std::ios_base::openmode mode = std::ios::out;
    if (append) {
        mode |= std::ios::app;  // режим добавления
    } else {
        mode |= std::ios::trunc; // режим замены
    }
    
    std::ofstream file(path, mode);
    if (!file.is_open()) {
        throw std::ios_base::failure("Failed to open file for writing");
    }
    
    file << full_name << " " << client_ID << " " << current_balance << "\n";
    file.close();
}

void BankUser::import_from_stream(std::ifstream& input_stream) {
    if (!input_stream.good()) {
        throw std::ios_base::failure("Input stream is not ready for reading");
    }
    
    // Сохраняем текущую позицию для отладки
    std::streampos start_pos = input_stream.tellg();
    
    // Читаем строку целиком
    std::string line;
    if (!std::getline(input_stream, line)) {
        throw std::ios_base::failure("Failed to read line from stream");
    }
    
    // Разбираем строку
    std::istringstream line_stream(line);
    
    // Читаем имя (может содержать пробелы)
    std::string name_part;
    std::string name;
    
    // Считываем части имени пока не встретим ID (который начинается с CLIENT)
    while (line_stream >> name_part) {
        // Проверяем, не является ли эта часть ID
        if (name_part.find("CLIENT") == 0) {
            // Это ID, возвращаем его в поток
            line_stream.seekg(-static_cast<int>(name_part.length()), std::ios_base::cur);
            break;
        }
        if (!name.empty()) name += " ";
        name += name_part;
    }
    
    // Читаем ID и баланс
    if (!(line_stream >> client_ID >> current_balance)) {
        throw std::ios_base::failure("Failed to parse ID and balance from: " + line);
    }
    
    full_name = name;
    
    // Проверяем, что имя не пустое
    if (full_name.empty()) {
        throw std::ios_base::failure("Empty name in imported data");
    }
}

std::string BankUser::generate_report() const {
    std::ostringstream oss;
    oss << "Клиент: " << full_name << ", ID: " << client_ID 
        << ", Баланс: " << current_balance;
    return oss.str();
}

void BankUser::validate_non_negative_sum(double sum, const std::string& operation) const {
    if (sum < 0) {
        throw std::invalid_argument("Negative " + operation + " is not allowed");
    }
}

void BankUser::validate_positive_sum(double sum, const std::string& operation) const {
    if (sum <= 0) {
        throw std::invalid_argument(operation + " sum must be positive");
    }
}

void BankUser::validate_sufficient_funds(double sum) const {
    if (current_balance < sum) {
        throw std::runtime_error("Insufficient funds on the account");
    }
}