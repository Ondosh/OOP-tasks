module com.github.ondosh.chatbot {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;


    opens com.github.ondosh.chatbot to javafx.fxml;
    exports com.github.ondosh.chatbot;
    exports com.github.ondosh.chatbot.controller;
    opens com.github.ondosh.chatbot.controller to javafx.fxml;
}