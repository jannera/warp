package com.rasanenj.warp.messaging;

import java.nio.ByteBuffer;

/**
 * @author gilead
 */
public class DisconnectMessage extends TextMessage {
    public DisconnectMessage(Player player) {
        super(player.getName());
    }

    public DisconnectMessage(ByteBuffer b) {
        super(b);
    }

    @Override
    public MessageType getType() {
        return MessageType.DISCONNECT;
    }
}
