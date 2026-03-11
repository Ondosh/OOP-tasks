module com.github.ondosh.trianglegui {
    requires javafx.controls;
    requires javafx.fxml;
    requires Triangle;


    opens com.github.ondosh.trianglegui to javafx.fxml;
    exports com.github.ondosh.trianglegui;
}