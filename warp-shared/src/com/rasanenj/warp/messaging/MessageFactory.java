package com.rasanenj.warp.messaging;

import java.nio.ByteBuffer;

/**
 * @author gilead
 */
public class MessageFactory {
    public static Message decode(ByteBuffer msg) {
        Message.MessageType type = Message.readType(msg);
        switch (type) {
            case JOIN_SERVER:
                return new JoinServerMessage(msg);
            case CHAT_MSG:
                return new ChatMessage(msg);
        }
        System.out.println("MessageFactory could not decode type " + type);
        return null;
    }

}
