package com.rasanenj.warp.messaging;

import java.nio.ByteBuffer;
import java.util.logging.Level;

import static com.rasanenj.warp.Log.log;

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
            case DISCONNECT:
                return new DisconnectMessage(msg);
            case UPDATE_SHIP_PHYSICS:
                return new ShipPhysicsMessage(msg);
            case CREATE_SHIP:
                return new CreateShipMessage(msg);
            case SET_ACCELERATION:
                return new AccelerationMessage(msg);
        }
        log(Level.SEVERE, "MessageFactory could not decode type " + type);
        return null;
    }

}
