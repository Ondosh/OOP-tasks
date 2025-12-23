// animal.cpp
#include "Animals.hpp"
#include <string>

// === Animal ===
Animal::Animal(const std::string& n) : name(n) {}

std::string Animal::getName() const {
    return name;
}

// По умолчанию — нейтральное поведение
void Animal::expressHappiness() const {
    std::cout << name << " выглядит довольным.\n";
}

// === Cat ===
Cat::Cat(const std::string& n, int l) : Animal(n), lives(l < 1 ? 1 : l) {}

void Cat::speak() const {
    std::cout << name << " говорит: Мяу!\n";
}

void Cat::move() const {
    std::cout << name << " идёт бесшумно.\n";
}

void Cat::purr() const {
    std::cout << name << " громко мурлычет!\n";
}

void Cat::expressHappiness() const {
    purr(); // переиспользуем уникальный метод
}

int Cat::getLifespan() const {
    return 12 + (lives > 0 ? lives / 3 : 0);
}

// === Dog ===
Dog::Dog(const std::string& n, const std::string& b) : Animal(n), breed(b) {}

void Dog::speak() const {
    std::cout << name << " (" << breed << ") говорит: Гав!\n";
}

void Dog::move() const {
    std::cout << name << " бежит энергично.\n";
}

void Dog::fetch() const {
    std::cout << name << " радостно приносит мяч!\n";
}

void Dog::expressHappiness() const {
    std::cout << name << " виляет хвостом от радости!\n";
}

int Dog::getLifespan() const {
    return breed == "Great Dane" ? 8 : 12;
}