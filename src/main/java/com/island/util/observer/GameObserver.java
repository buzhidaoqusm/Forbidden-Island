package com.island.util.observer;

import com.island.models.Player;
import com.island.models.Position;
import com.island.models.GameState;

public interface GameObserver {
    void onGameStateChanged(GameState state);
    void onBoardChanged();
    void onPlayerMoved(Player player, Position newPosition);
    void onWaterLevelChanged(int newLevel);
    void onCardChanged();
    void onPlayerInfoChanged();
    void onActionBarChanged();
} 