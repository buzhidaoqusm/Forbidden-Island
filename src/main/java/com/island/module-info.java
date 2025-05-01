module com.island.forbiddenisland {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.island to javafx.fxml;
    exports com.island;
}