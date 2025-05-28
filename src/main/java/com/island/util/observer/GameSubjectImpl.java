package com.island.util.observer;

import com.island.model.Player;
import com.island.model.Position;
import com.island.model.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameSubjectImpl implements GameSubject {
    private final List<GameObserver> observers;
    private volatile GameState gameState;
    
    public GameSubjectImpl() {
        // 使用线程安全的列表实现
        observers = new CopyOnWriteArrayList<>();
        gameState = GameState.INITIALIZING;
    }
    
    @Override
    public void addObserver(GameObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
            // 立即通知新添加的观察者当前状态
            observer.onGameStateChanged(gameState);
        }
    }
    
    @Override
    public void removeObserver(GameObserver observer) {
        if (observer != null) {
            observers.remove(observer);
        }
    }
    
    @Override
    public void notifyObservers() {
        GameState currentState = gameState;
        for (GameObserver observer : observers) {
            try {
                observer.onGameStateChanged(currentState);
            } catch (Exception e) {
                System.err.println("Error notifying observer: " + e.getMessage());
            }
        }
    }
    
    public void notifyBoardChanged() {
        for (GameObserver observer : observers) {
            try {
                observer.onBoardChanged();
            } catch (Exception e) {
                System.err.println("Error notifying board change: " + e.getMessage());
            }
        }
    }
    
    public void notifyPlayerMoved(Player player, Position newPosition) {
        for (GameObserver observer : observers) {
            try {
                observer.onPlayerMoved(player, newPosition);
            } catch (Exception e) {
                System.err.println("Error notifying player move: " + e.getMessage());
            }
        }
    }
    
    public void notifyWaterLevelChanged(int newLevel) {
        for (GameObserver observer : observers) {
            try {
                observer.onWaterLevelChanged(newLevel);
            } catch (Exception e) {
                System.err.println("Error notifying water level change: " + e.getMessage());
            }
        }
    }
    
    public void notifyCardChanged() {
        for (GameObserver observer : observers) {
            try {
                observer.onCardChanged();
            } catch (Exception e) {
                System.err.println("Error notifying card change: " + e.getMessage());
            }
        }
    }
    
    public void notifyPlayerInfoChanged() {
        for (GameObserver observer : observers) {
            try {
                observer.onPlayerInfoChanged();
            } catch (Exception e) {
                System.err.println("Error notifying player info change: " + e.getMessage());
            }
        }
    }
    
    public void notifyActionBarChanged() {
        for (GameObserver observer : observers) {
            try {
                observer.onActionBarChanged();
            } catch (Exception e) {
                System.err.println("Error notifying action bar change: " + e.getMessage());
            }
        }
    }
    
    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState newState) {
        if (newState != null && this.gameState != newState) {
            this.gameState = newState;
            System.out.println("Game state changed to: " + newState);
            notifyObservers();
        }
    }

    public int getObserverCount() {
        return observers.size();
    }
}