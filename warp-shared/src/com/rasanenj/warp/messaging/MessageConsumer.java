package com.rasanenj.warp.messaging;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author gilead
 */
public abstract class MessageConsumer {
    public MessageConsumer(MessageDelegator delegator) {
        for(Message.MessageType type : getMessageTypes()) {
            delegator.register(this, type);
        }
    }

    // TODO: get rid of this once Messages either contain a player or they dont
    private class MessageAndPlayer {
        public MessageAndPlayer(Player player, Message message) {
            this.player = player;
            this.message = message;
        }
        public Player player;
        public Message message;
    }

    private ArrayList<MessageAndPlayer> storedMessages = new ArrayList<MessageAndPlayer>();

    public abstract void consume(Player player, Message msg);
    // TODO: make possible player part of the message, and remove it from here

    public abstract Collection<Message.MessageType> getMessageTypes();

    // TODO: change this to implemented function, and add abstract
    // Collection<MessageType> getMessageTypeRegistration()
    // then consider making calling this register-function somehow automatic

    public synchronized void store(Player player, Message message) {
        storedMessages.add(new MessageAndPlayer(player, message));
    }

    public synchronized void consumeStoredMessages() {
        for (MessageAndPlayer pair : storedMessages) {
            consume(pair.player, pair.message);
        }
        storedMessages.clear();
    }
}
