package com.island.view;

/**
 * Action type enumeration, used to identify different action types available in the game
 */
public enum ActionType { 
    /**
     * Move action
     */
    MOVE, 
    
    /**
     * Shore up action
     */
    SHORE_UP, 
    
    /**
     * Give card action
     */
    GIVE_CARD, 
    
    /**
     * Capture treasure action
     */
    CAPTURE_TREASURE, 
    
    /**
     * Use special ability action
     */
    USE_ABILITY, 
    
    /**
     * End turn action
     */
    END_TURN 
} 