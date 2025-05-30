module com.island {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;

    opens com.island.network to com.fasterxml.jackson.databind;
    opens com.island.models to com.fasterxml.jackson.databind;
    
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