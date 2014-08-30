package com.rasanenj.warp.messaging;

import com.rasanenj.warp.entities.ShipStats;

import java.nio.ByteBuffer;

/**
 * @author gilead
 */
public class ScoreGatheringPointMessage extends Message {
    private final float y, x;

    public ScoreGatheringPointMessage(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public ScoreGatheringPointMessage(ByteBuffer msg) {
        this.x = msg.getFloat();
        this.y = msg.getFloat();
    }

    @Override
    public MessageType getType() {
        return MessageType.CREATE_SCORE_GATHERING_POINT;
    }

    @Override
    public byte[] encode() {
        ByteBuffer b = create(2 * Float.SIZE/8);
        b.putFloat(x).putFloat(y);
        return b.array();
    }

    public float getY() {
        return y;
    }

    public float getX() {
        return x;
    }
}
