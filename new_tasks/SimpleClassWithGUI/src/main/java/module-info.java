module com.github.ondosh.simpleclasswithgui {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.github.ondosh.simpleclasswithgui to javafx.fxml;
    exports com.github.ondosh.simpleclasswithgui;
}