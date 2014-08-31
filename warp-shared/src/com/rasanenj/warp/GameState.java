package com.rasanenj.warp;

/**
 * @author gilead
 */
public enum GameState {
    RUNNING, PAUSED;

    public static GameState getState(int i) {
        return GameState.values()[i];
    }
}
