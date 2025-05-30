package com.island.util.observer;

import com.island.models.adventurers.Player;
import com.island.models.game.GameState;
import com.island.models.island.Position;

import java.util.ArrayList;
import java.util.List;

public class GameSubjectImpl implements GameSubject {
    private List<GameObserver> observers;
    private GameState gameState;

    public GameSubjectImpl() {
        observers = new ArrayList<>();
        gameState = GameState.INITIALIZING;
    }

    @Override
    public void addObserver(GameObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (GameObserver observer : observers) {
            observer.onGameStateChanged(gameState);
        }
    }

    public void notifyBoardChanged() {
        for (GameObserver observer : observers) {
            observer.onBoardChanged();
        }
    }

    public void notifyPlayerMoved(Player player, Position newPosition) {
        for (GameObserver observer : observers) {
            observer.onPlayerMoved(player, newPosition);
        }
    }

    public void notifyWaterLevelChanged(int newLevel) {
        for (GameObserver observer : observers) {
            observer.onWaterLevelChanged(newLevel);
        }
    }

    public void notifyCardChanged() {
        for (GameObserver observer : observers) {
            observer.onCardChanged();
        }
    }

    public void notifyPlayerInfoChanged() {
        for (GameObserver observer : observers) {
            observer.onPlayerInfoChanged();
        }
    }

    public void notifyActionBarChanged() {
        for (GameObserver observer : observers) {
            observer.onActionBarChanged();
        }
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        notifyObservers();
    }
}