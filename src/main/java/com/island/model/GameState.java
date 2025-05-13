package com.island.model;

public enum GameState {
    ONGOING,                    // 游戏进行中
    VICTORY,                    // 胜利
    DEFEAT_WATER_LEVEL,        // 水位过高失败
    DEFEAT_TREASURE_LOST,      // 宝藏丢失失败
    DEFEAT_FOOLS_LANDING_SUNK, // 愚者降临点沉没失败
    DEFEAT_PLAYERS_TRAPPED     // 玩家被困失败
}