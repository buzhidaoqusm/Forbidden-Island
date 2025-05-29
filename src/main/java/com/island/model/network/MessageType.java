package com.forbiddenisland.models.network;

public enum MessageType {
    MOVE_PLAYER,                // 移动玩家
    SHORE_UP,                   // 加固板块
    GIVE_CARD,                  // 给予卡牌
    CAPTURE_TREASURE,           // 获取宝藏
    DRAW_TREASURE_CARD,         // 抽取宝藏卡
    DRAW_FLOOD_CARD,            // 抽取洪水卡
    GAME_START,                 // 游戏开始
    TURN_START,                 // 回合开始
    PLAYER_JOIN,                // 玩家加入
    PLAYER_LEAVE,               // 玩家离开
    UPDATE_ROOM,                // 更新房间信息
    END_TURN,                   // 结束回合
    DISCARD_CARD,               // 弃牌
    MOVE_PLAYER_BY_NAVIGATOR,   // 航海家移动玩家
    HELICOPTER_MOVE,            // 直升机移动
    SANDBAGS_USE,               // 使用沙袋
    GAME_OVER,                  // 游戏结束
    LEAVE_ROOM,                 // 离开房间
    MESSAGE_ACK,                // 消息确认
}
