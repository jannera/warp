package com.rasanenj.warp.messaging;

import com.rasanenj.warp.GameState;

import java.nio.ByteBuffer;

/**
 * @author gilead
 */
public class GameStateChangeMessage extends Message {
    private final GameState newState;
    private final int lengthInSec; // 0 = indefinite

    public GameStateChangeMessage(GameState newState, int lengthInSec) {
        this.newState = newState;
        this.lengthInSec = lengthInSec;
    }

    public GameStateChangeMessage(ByteBuffer b) {
        this.newState = GameState.getState(b.getInt());
        this.lengthInSec = b.getInt();
    }

    @Override
    public MessageType getType() {
        return MessageType.GAME_STATE_CHANGE;
    }

    @Override
    public byte[] encode() {
        ByteBuffer b = create(2 *Integer.SIZE/8);
        b.putInt(newState.ordinal()).putInt(lengthInSec);
        return b.array();
    }

    public GameState getNewState() {
        return newState;
    }

    public int getLengthInSec() {
        return lengthInSec;
    }
}
