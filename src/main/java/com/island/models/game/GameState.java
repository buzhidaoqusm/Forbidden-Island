package com.island.models.game;

/**
 * Represents the different states of the game.
 * These states control the flow and progression of the game.
 */
public enum GameState {
    WAITING,      // Game is waiting for players to join
    INITIALIZING, // Game is being set up
    RUNNING,      // Game is in progress
    PAUSED,       // Game is temporarily paused
    GAME_OVER,    // Game has ended
    TURN_START,   // A new turn is starting
    TURN_END      // A turn has ended
} 