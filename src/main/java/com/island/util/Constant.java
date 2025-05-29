package com.island.util;

import java.util.HashMap;
import java.util.Map;

public class Constant {
    // 标准化的瓦片名称格式
    public static final String FOOLS_LANDING = "FOOL'S LANDING";
    public static final String BRONZE_GATE = "BRONZE GATE";
    public static final String COPPER_GATE = "COPPER GATE";
    public static final String SILVER_GATE = "SILVER GATE";
    public static final String GOLD_GATE = "GOLD GATE";
    public static final String IRON_GATE = "IRON GATE";
    public static final String TEMPLE_OF_MOON = "TEMPLE OF THE MOON";
    public static final String TEMPLE_OF_SUN = "TEMPLE OF THE SUN";
    public static final String CAVE_OF_EMBERS = "CAVE OF EMBERS";
    public static final String CAVE_OF_SHADOWS = "CAVE OF SHADOWS";

    public static final Map<String, String> tileNameMap = new HashMap<>() {{
        put("Black", IRON_GATE);
        put("Blue", FOOLS_LANDING);
        put("Earth1", TEMPLE_OF_MOON);
        put("Earth2", TEMPLE_OF_SUN);
        put("Fire1", CAVE_OF_EMBERS);
        put("Fire2", CAVE_OF_SHADOWS);
        put("Green", COPPER_GATE);
        put("Normal1", "BREAKERS BRIDGE");
        put("Normal2", "CLIFFS OF ABANDON");
        put("Normal3", "CRIMSON FOREST");
        put("Normal4", "DUNES OF DECEPTION");
        put("Normal5", "LOST LAGOON");
        put("Normal6", "OBSERVATORY");
        put("Normal7", "PHANTOM ROCK");
        put("Normal8", "MISTY MARSH");
        put("Normal9", "TWILIGHT HOLLOW");
        put("Normal10", "WATCHTOWER");
        put("Ocean1", "CORAL PALACE");
        put("Ocean2", "TIDAL PALACE");
        put("Red", BRONZE_GATE);
        put("White", SILVER_GATE);
        put("Wind1", "HOWLING GARDEN");
        put("Wind2", "WHISPERING GARDEN");
        put("Yellow", GOLD_GATE);
    }};

    public static final String[] tilesNames = {"Black", "Blue", "Green", "Red", "White", "Yellow", "Earth1", "Earth2",
            "Fire1", "Fire2", "Ocean1", "Ocean2", "Wind1", "Wind2", "Normal1", "Normal2", "Normal3", "Normal4",
            "Normal5", "Normal6", "Normal7", "Normal8", "Normal9", "Normal10"};

    /**
     * 标准化瓦片名称格式
     * @param tileName 输入的瓦片名称
     * @return 标准化后的瓦片名称
     */
    public static String standardizeTileName(String tileName) {
        if (tileName == null) return null;
        
        // 首先检查是否在映射表中
        for (Map.Entry<String, String> entry : tileNameMap.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(tileName) || entry.getKey().equalsIgnoreCase(tileName)) {
                return entry.getValue();
            }
        }
        
        // 如果不在映射表中,转换为大写
        return tileName.toUpperCase();
    }

    /**
     * 检查瓦片名称是否有效
     * @param tileName 要检查的瓦片名称
     * @return 如果是有效的瓦片名称则返回true
     */
    public static boolean isValidTileName(String tileName) {
        if (tileName == null) return false;
        
        // 检查是否在映射表中
        for (String value : tileNameMap.values()) {
            if (value.equalsIgnoreCase(tileName)) {
                return true;
            }
        }
        return false;
    }
}
