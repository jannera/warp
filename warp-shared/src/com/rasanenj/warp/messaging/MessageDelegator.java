package com.rasanenj.warp.messaging;

import com.badlogic.gdx.utils.Array;

import java.util.EnumMap;
import java.util.logging.Level;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 *
 */
public class MessageDelegator {

    /**
     * Registers given MessageConsumer to listen for all messages of the given type
     */
    public void register(MessageConsumer consumer, Message.MessageType type) {
        Array<MessageConsumer> consumers = consumerLists.get(type);
        if (consumers == null) {
            consumers = new Array<MessageConsumer>(1);
            consumerLists.put(type, consumers);
        }
        consumers.add(consumer);
    }

    /**
     * Delegates message to all MessageConsumers who have registered to the type of the message
     */
    public void delegate(Player player, Message msg) {
        Message.MessageType type = msg.getType();
        Array<MessageConsumer> consumers = consumerLists.get(type);
        if (consumers == null) {
            log(Level.SEVERE, "No consumer list for message of type " + type);
            return;
        }
        if (consumers.size == 0) {
            log(Level.SEVERE, "Empty consumer list for message of type " + type);
            return;
        }
        for (MessageConsumer c : consumers) {
            c.store(player, msg);
        }
    }

    final EnumMap<Message.MessageType, Array<MessageConsumer>> consumerLists =
            new EnumMap<Message.MessageType, Array<MessageConsumer>>(Message.MessageType.class);
}
