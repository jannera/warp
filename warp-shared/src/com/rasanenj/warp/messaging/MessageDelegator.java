package com.rasanenj.warp.messaging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        Collection<MessageConsumer> consumers = consumerLists.get(type);
        if (consumers == null) {
            consumers = new ArrayList<MessageConsumer>(1);
            consumerLists.put(type, consumers);
        }
        consumers.add(consumer);
    }

    /**
     * Delegates message to all MessageConsumers who have registered to the type of the message
     */
    public void delegate(Player player, Message msg) {
        Message.MessageType type = msg.getType();
        Collection<MessageConsumer> consumers = consumerLists.get(type);
        Logger logger = Logger.getLogger("WarpGame");
        logger.log(Level.WARNING, type.name() + ":" + consumers);
        if (consumers == null) {
            log(Level.SEVERE, "No consumer list for message of type " + type);
            return;
        }
        if (consumers.isEmpty()) {
            log(Level.SEVERE, "Empty consumer list for message of type " + type);
            return;
        }
        for (MessageConsumer c : consumers) {
            c.consume(player, msg);
        }
    }

    final EnumMap<Message.MessageType, Collection<MessageConsumer>> consumerLists =
            new EnumMap<Message.MessageType, Collection<MessageConsumer>>(Message.MessageType.class);
}
