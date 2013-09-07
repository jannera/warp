package com.rasanenj.warp.messaging;

import java.nio.ByteBuffer;

/**
 * @author gilead
 */
public class DisconnectMsg extends TextMessage {
    public DisconnectMsg(Player player) {
        super(player.getName());
    }

    public DisconnectMsg(ByteBuffer b) {
        super(b);
    }

    @Override
    public MessageType getType() {
        return MessageType.DISCONNECT_MSG;
    }
}
