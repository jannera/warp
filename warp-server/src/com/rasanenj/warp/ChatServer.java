package com.rasanenj.warp;

import com.rasanenj.warp.messaging.*;

import java.util.Arrays;
import java.util.Collection;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class ChatServer {
    private final ChatMessageConsumer consumer;

    private class ChatMessageConsumer extends MessageConsumer {
        public ChatMessageConsumer(MessageDelegator delegator) {
            super(delegator);
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
        public Collection<Message.MessageType> getMessageTypes() {
            return Arrays.asList(Message.MessageType.JOIN_SERVER,
                    Message.MessageType.CHAT_MSG);
        }
    }

    private final WSServer server;

    public ChatServer(WSServer server, MessageDelegator delegator) {
        this.server = server;
        this.consumer = new ChatMessageConsumer(delegator);
    }

    // TODO: needs somekind of update, to be able to consume stored messages
}
