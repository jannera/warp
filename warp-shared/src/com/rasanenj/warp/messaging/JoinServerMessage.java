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

    public JoinServerMessage(ByteBuffer b) {
        id = b.getLong();
        byte[] newArr = new byte[b.remaining()];
        b.get(newArr);
        playerName = Message.decode(newArr);
    }

    public JoinServerMessage(String playerName, long id) {
        this.playerName = playerName;
        this.id = id;
    }

    @Override
    public MessageType getType() {
        return MessageType.JOIN_SERVER;
    }

    @Override
    public byte[] encode() {
        byte[] nameInBytes = Message.encode(playerName);

        ByteBuffer b = create(Long.SIZE/8 + nameInBytes.length);
        b.putLong(id);
        b.put(nameInBytes);

        return b.array();
    }

    public String getPlayerName() {
        return playerName;
    }

    public long getId() {
        return id;
    }
}
