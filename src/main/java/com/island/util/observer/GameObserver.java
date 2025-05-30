package com.island.util.observer;

import com.island.models.adventurers.Player;
import com.island.models.game.GameState;
import com.island.models.island.Position;

public interface GameObserver {
    void onGameStateChanged(GameState state);
    void onBoardChanged();
    void onPlayerMoved(Player player, Position newPosition);
    void onWaterLevelChanged(int newLevel);
    void onCardChanged();
    void onPlayerInfoChanged();
    void onActionBarChanged();
} 