package com.rasanenj.warp.messaging;

import java.nio.ByteBuffer;

/**
 * @author gilead
 */
public class ScoreUpdateMessage extends Message {
    private float score;
    private long playerId;

    public ScoreUpdateMessage(long playerId, float score) {
        this.playerId = playerId;
        this.score = score;
    }

    public ScoreUpdateMessage(ByteBuffer b) {
        this.playerId = b.getLong();
        this.score = b.getFloat();
    }

    @Override
    public MessageType getType() {
        return MessageType.SCORE_UPDATE;
    }

    @Override
    public byte[] encode() {
        ByteBuffer b = create(Long.SIZE/8 + Float.SIZE/8);
        b.putLong(playerId).putFloat(score);
        return b.array();
    }

    public float getScore() {
        return score;
    }

    public void update(Player p) {
        this.playerId = p.getId();
        this.score = p.getScore();
    }

    public long getPlayerId() {
        return playerId;
    }
}
