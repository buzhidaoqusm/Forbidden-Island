module com.island {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;

    opens com.island.launcher to javafx.fxml;
    exports com.island.controller;
    exports com.island.models;
    exports com.island.network;
    exports com.island.util;
    exports com.island.util.observer;
    exports com.island.util.ui;
    exports com.island.view;
    exports com.island.launcher;
    exports com.island.controller.factory;
}