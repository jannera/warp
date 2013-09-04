package com.rasanenj.warp.messaging;

import java.nio.ByteBuffer;

/**
 * @author gilead
 */
public class JoinServerMessage extends StringMessage {
    public JoinServerMessage(ByteBuffer b) {
        super(b);
    }

    public JoinServerMessage(String playerName) {
        super(playerName);
    }

    @Override
    public MessageType getType() {
        return MessageType.JOIN_SERVER;
    }
}
