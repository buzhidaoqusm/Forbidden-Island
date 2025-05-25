module com.island {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires com.fasterxml.jackson.databind;

    opens com.island.launcher to javafx.fxml;
    opens com.island.view to javafx.fxml, javafx.graphics;
    exports com.island.view;
    exports com.island.controller;
    exports com.island.model;
    exports com.island.network;
}