package com.rasanenj.warp.messaging;

/**
 * @author gilead
 */
public interface MessageConsumer {
    public abstract void consume(Player player, Message msg);

    public abstract void register(MessageDelegator delegator);
}
