package com.rasanenj.warp;

import com.rasanenj.warp.messaging.*;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class ChatServer implements MessageConsumer {
    private final WSServer server;

    public ChatServer(WSServer server) {
        this.server = server;
    }

    @Override
    public void consume(Player player, Message message) {
        log("ChatServer: Received " + message.getType());
        if (message.getType() == Message.MessageType.JOIN_SERVER) {
            server.sendToAll(message);
        }
        else if (message.getType() == Message.MessageType.CHAT_MSG) {
            ((ChatMessage) message).addNick(player.getName());
            server.sendToAll(message);
        }
    }

    @Override
    public void register(MessageDelegator delegator) {
        delegator.register(this, Message.MessageType.JOIN_SERVER);
        delegator.register(this, Message.MessageType.CHAT_MSG);
    }
}
