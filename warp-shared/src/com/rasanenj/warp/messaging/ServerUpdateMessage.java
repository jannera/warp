package com.rasanenj.warp.messaging;

import java.nio.ByteBuffer;

/**
 * @author gilead
 */
public class ServerUpdateMessage extends TextMessage {
    public ServerUpdateMessage(String msg) {
        super(msg);
    }

    public ServerUpdateMessage(ByteBuffer b) {
        super(b);
    }

    @Override
    public MessageType getType() {
        return MessageType.SERVER_UPDATE;
    }
}
