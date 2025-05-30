package com.forbiddenisland.launcher;

import com.forbiddenisland.models.adventurers.Player;
import com.forbiddenisland.views.ui.MenuView;

import javafx.application.Application;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

public class GameStart extends Application {

    @Override
    public void start(Stage primaryStage) {

        // 创建文本输入对话框
        TextInputDialog dialog = new TextInputDialog("Username");
        dialog.setTitle("Enter your name");
        dialog.setHeaderText("Please enter your name.");

        // 在对话框隐藏时处理用户名
        dialog.setOnHiding(evt -> {
            String result = dialog.getResult();
            if (result != null && !result.trim().isEmpty()) {
                Player player = new Player(result);
                // 用户名输入完成后，加载主界面
                loadMainMenu(primaryStage, player);
            } else {
                // 如果没有输入用户名，仍然要求输入
                dialog.show();
            }
        });

        // 显示输入对话框
        dialog.show();
    }

    // 加载主界面的方法
    private void loadMainMenu(Stage primaryStage, Player player) {
        // 初始化视图
        MenuView mainView = new MenuView();
        // 设置场景并显示
        primaryStage.setScene(mainView.getMenuScene(primaryStage, player));
        primaryStage.setTitle("Forbidden Island");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
