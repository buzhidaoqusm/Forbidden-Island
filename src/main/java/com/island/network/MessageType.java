package com.island.network;

public enum MessageType {
    MOVE_PLAYER,                // move the player
    SHORE_UP,                   // shore up tiles
    GIVE_CARD,                  // give the card
    CAPTURE_TREASURE,           // capture the treasure
    DRAW_TREASURE_CARD,         // draw treasure card
    DRAW_FLOOD_CARD,            // draw flood card
    GAME_START,                 // game start
    TURN_START,                 // turn start
    PLAYER_JOIN,                // player join in the room
    PLAYER_LEAVE,               // player leave the room
    UPDATE_ROOM,                // update the room information
    END_TURN,                   // end the turn
    DISCARD_CARD,               // discard the card
    MOVE_PLAYER_BY_NAVIGATOR,   // navigator move the player
    HELICOPTER_MOVE,            // helicopter's move
    SANDBAGS_USE,               // use sandbag
    GAME_OVER,                  // game end
    LEAVE_ROOM,                 // leave the room
    MESSAGE_ACK,                // confirm the message
}
