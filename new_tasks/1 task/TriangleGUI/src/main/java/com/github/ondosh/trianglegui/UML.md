classDiagram
class MainApplication
class MainController
class Triangle

    MainApplication ..> MainController : загружает
    MainController -- Triangle : использует