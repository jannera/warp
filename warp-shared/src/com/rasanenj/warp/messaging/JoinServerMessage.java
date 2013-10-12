package com.rasanenj.warp.messaging;

import java.nio.ByteBuffer;

/**
 * @author gilead
 *
 * TODO: separate JOIN_BATTLE and JOIN_CHAT
 */
public class JoinServerMessage extends Message {
    final String playerName;
    final long id;
    private final int colorIndex;

    public JoinServerMessage(ByteBuffer b) {
        id = b.getLong();
        colorIndex = b.getInt();
        byte[] newArr = new byte[b.remaining()];
        b.get(newArr);
        playerName = Message.decode(newArr);
    }

    public JoinServerMessage(String playerName, long id, int colorIndex) {
        this.playerName = playerName;
        this.id = id;
        this.colorIndex = colorIndex;
    }

    @Override
    public MessageType getType() {
        return MessageType.JOIN_SERVER;
    }

    @Override
    public byte[] encode() {
        byte[] nameInBytes = Message.encode(playerName);

        ByteBuffer b = create(Long.SIZE/8 + Integer.SIZE/8 + nameInBytes.length);
        b.putLong(id);
        b.putInt(colorIndex);
        b.put(nameInBytes);

        return b.array();
    }

    public String getPlayerName() {
        return playerName;
    }

    public long getId() {
        return id;
    }

    public int getColorIndex() {
        return colorIndex;
    }
}
