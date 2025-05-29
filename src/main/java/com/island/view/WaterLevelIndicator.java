package com.island.view;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class WaterLevelIndicator extends VBox {
    private final ProgressBar progressBar;
    private final Label levelLabel;
    private static final int MAX_WATER_LEVEL = 10;

    public WaterLevelIndicator() {
        // 设置间距
        setSpacing(5);

        // 创建标签
        Label titleLabel = new Label("水位");
        titleLabel.setTextFill(Color.BLUE);

        // 创建进度条
        progressBar = new ProgressBar(0);
        progressBar.setStyle("-fx-accent: #0000FF;"); // 蓝色进度条
        progressBar.setPrefWidth(150);

        // 创建水位数值标签
        levelLabel = new Label("水位: 1/" + MAX_WATER_LEVEL);

        // 添加组件
        getChildren().addAll(titleLabel, progressBar, levelLabel);
    }

    /**
     * 更新水位显示
     * @param level 当前水位（1-10）
     */
    public void update(int level) {
        // 确保水位在有效范围内
        int validLevel = Math.max(1, Math.min(level, MAX_WATER_LEVEL));
        
        // 更新进度条（转换为0-1范围）
        double progress = (validLevel - 1.0) / (MAX_WATER_LEVEL - 1.0);
        progressBar.setProgress(progress);
        
        // 更新标签
        levelLabel.setText("水位: " + validLevel + "/" + MAX_WATER_LEVEL);

        // 根据水位设置颜色
        if (validLevel <= 3) {
            progressBar.setStyle("-fx-accent: #0000FF;"); // 蓝色
        } else if (validLevel <= 6) {
            progressBar.setStyle("-fx-accent: #FFA500;"); // 橙色
        } else {
            progressBar.setStyle("-fx-accent: #FF0000;"); // 红色
        }
    }
} 