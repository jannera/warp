package com.rasanenj.warp.messaging;

/**
 * @author gilead
 */
public interface MessageConsumer {
    public abstract void consume(Player player, Message msg);
    // TODO: make possible player part of the message, and remove it from here

    public abstract void register(MessageDelegator delegator);
    // TODO: change this to implemented function, and add abstract
    // Collection<MessageType> getMessageTypeRegistration()
    // then consider making calling this register-function somehow automatic
}
