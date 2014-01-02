package com.rasanenj.warp.messaging;

import java.nio.ByteBuffer;

/**
 * @author gilead
 */
public class JoinChatMessage extends TextMessage {
    public JoinChatMessage(String msg) {
        super(msg);
    }

    public JoinChatMessage(ByteBuffer b) {
        super(b);
    }

    @Override
    public MessageType getType() {
        return MessageType.JOIN_CHAT;
    }
}
