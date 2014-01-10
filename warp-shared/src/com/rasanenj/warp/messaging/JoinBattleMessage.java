package com.rasanenj.warp.messaging;

import java.nio.ByteBuffer;

/**
 * @author Janne Rasanen
 */
public class JoinBattleMessage extends Message {
    private String playerName;
    private final long playerId;
    private final int colorIndex;

    public JoinBattleMessage(String playerName, long playerId, int colorIndex) {
        this.playerName = playerName;
        this.playerId = playerId;
        this.colorIndex = colorIndex;
    }

    public JoinBattleMessage(ByteBuffer b) {
        playerId = b.getLong();
        colorIndex = b.getInt();
        byte[] newArr = new byte[b.remaining()];
        b.get(newArr);
        playerName = Message.decode(newArr);
    }

    @Override
    public MessageType getType() {
        return MessageType.JOIN_BATTLE;
    }

    @Override
    public byte[] encode() {
        byte[] nameInBytes = Message.encode(playerName);
        ByteBuffer b = create(nameInBytes.length + Long.SIZE/8 + Integer.SIZE/8);
        b.putLong(playerId);
        b.putInt(colorIndex);
        b.put(nameInBytes);
        return b.array();
    }

    public long getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getColorIndex() {
        return colorIndex;
    }
}
