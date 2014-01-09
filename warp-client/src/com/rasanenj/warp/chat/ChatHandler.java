package com.rasanenj.warp.chat;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.rasanenj.warp.messaging.*;

import java.util.Arrays;
import java.util.Collection;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class ChatHandler {
    private final ChatMessageConsumer consumer;
    private MessageListener listener;

    private class ChatMessageConsumer extends MessageConsumer {
        public ChatMessageConsumer(MessageDelegator delegator) {
            super(delegator);
        }

        @Override
        public void consume(Player player, Message msg) {
            String chatMsg;

            log(msg.getType());

            if (msg.getType() == Message.MessageType.JOIN_CHAT) {
                chatMsg = ((JoinChatMessage) msg).getMsg() + " joined channel";
            }
            else if (msg.getType() == Message.MessageType.DISCONNECT) {
                chatMsg = ((DisconnectMessage) msg).getMsg() + " left channel";
            }
            else if (msg.getType() == Message.MessageType.CHAT_MSG) {
                chatMsg = ((TextMessage) msg).getMsg();
            }
            else {
                return;
            }

            listener.handle(chatMsg);
        }

        @Override
        public Collection<Message.MessageType> getMessageTypes() {
            return Arrays.asList(Message.MessageType.JOIN_CHAT,
                    Message.MessageType.CHAT_MSG, Message.MessageType.DISCONNECT);
        }
    }

    private final ServerConnection serverConnection;

    public void send(String message) {
        serverConnection.send(new ChatMessage(message));
    }

    public ChatHandler(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
        this.consumer = new ChatMessageConsumer(serverConnection.getDelegator());
    }

    public void setListener(MessageListener listener) {
        this.listener = listener;
    }

    public void processArrivedMessages() {
        consumer.consumeStoredMessages();
    }
}
