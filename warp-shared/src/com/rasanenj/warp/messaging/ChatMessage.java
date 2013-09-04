package com.rasanenj.warp.messaging;

import java.nio.ByteBuffer;

/**
 * @author gilead
 */
public class ChatMessage extends StringMessage {
    public ChatMessage(String msg) {
        super(msg);
    }

    public ChatMessage(ByteBuffer b) {
        super(b);
    }

    @Override
    public MessageType getType() {
        return MessageType.CHAT_MSG;
    }

    public void addNick(String nick) {
        msg = "<" + nick + "> " + msg;
    }
}
