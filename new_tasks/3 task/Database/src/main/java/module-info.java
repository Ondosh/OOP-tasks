module com.github.ondosh.database {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;

    // Открываем пакеты для JavaFX
    opens com.github.ondosh.database to javafx.fxml;
    opens com.github.ondosh.database.controller to javafx.fxml;
    opens com.github.ondosh.database.model to javafx.base; // ← вот это главное!

    exports com.github.ondosh.database;
}