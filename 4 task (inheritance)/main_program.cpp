// main.cpp
#include "Animals.hpp"
#include <memory>
#include <vector>

int main() {
    std::vector<std::unique_ptr<Animal>> pets;
    pets.push_back(std::make_unique<Cat>("Kitty", 7));
    pets.push_back(std::make_unique<Dog>("Doggy", "Golden Retriever"));
    pets.push_back(std::make_unique<Dog>("Bobik", "Great Dane"));
    std::cout << "Общее поведение через полиморфизм\n";
    for (const auto& pet : pets) {
        std::cout << "Name: " << pet->getName() << "\n";
        pet->speak();
        pet->move();
        pet->expressHappiness(); // Полиморфный вызов
        std::cout << "Lifespan: " << pet->getLifespan() << " years\n\n";
    }

    std::cout << "Специфичное поведение через dynamic_cast\n";
    // Пытаемся вызвать уникальные методы
    // Необходимо вызывать через dynamic_cast, т.к. в самом Animal отсутствует метод, специфичный для классов наследников.
    if (Cat* c = dynamic_cast<Cat*>(pets[0].get())) {
        c->purr(); 
    }
    if (Dog* d = dynamic_cast<Dog*>(pets[1].get())) {
        d->fetch();
    }

    return 0;
}