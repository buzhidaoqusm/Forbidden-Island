package com.island.view;

import com.island.controller.GameController;
import java.lang.reflect.Field;

/**
 * 辅助类，用于访问 IslandView 的私有属性
 */
public class IslandViewHelper {

    /**
     * 获取 IslandView 中的 GameController 实例
     * @param islandView IslandView 实例
     * @return GameController 实例
     * @throws Exception 如果无法访问 GameController
     */
    public static GameController getGameController(IslandView islandView) throws Exception {
        Field field = IslandView.class.getDeclaredField("gameController");
        field.setAccessible(true);
        return (GameController) field.get(islandView);
    }
    
    /**
     * 调用 IslandView 的 updateWaterLevel 方法
     * @param islandView IslandView 实例
     * @param level 水位线级别
     * @throws Exception 如果无法调用方法
     */
    public static void updateWaterLevel(IslandView islandView, int level) throws Exception {
        java.lang.reflect.Method method = IslandView.class.getDeclaredMethod("updateWaterLevel", int.class);
        method.setAccessible(true);
        method.invoke(islandView, level);
    }
} 