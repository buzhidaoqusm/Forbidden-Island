package com.island.util.observer;

import com.island.model.Player;
import com.island.model.Position;
import com.island.model.GameState;

public interface GameObserver {
    void onGameStateChanged(GameState state);
    void onBoardChanged();
    void onPlayerMoved(Player player, Position newPosition);
    void onWaterLevelChanged(int newLevel);
    void onCardChanged();
    void onPlayerInfoChanged();
    void onActionBarChanged();
} 