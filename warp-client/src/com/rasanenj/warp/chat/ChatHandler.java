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
    private final ChatMessageListener listener;

    private class ChatMessageListener implements TextField.TextFieldListener {
        @Override
        public void keyTyped(TextField textField, char key) {
            if (key == '\n' || key == '\r') {
                String msg = textField.getText();
                if (msg.isEmpty()) {
                    return;
                }
                serverConnection.send(new ChatMessage(msg));
                textField.setText("");
            }
        }
    }

    private class ChatMessageConsumer extends MessageConsumer {
        public ChatMessageConsumer(MessageDelegator delegator) {
            super(delegator);
        }

        @Override
        public void consume(Player player, Message msg) {
            String chatMsg;

            log(msg.getType());

            if (msg.getType() == Message.MessageType.JOIN_SERVER) {
                chatMsg = ((TextMessage) msg).getMsg() + " joined channel";
            }
            else if (msg.getType() == Message.MessageType.DISCONNECT) {
                chatMsg = ((TextMessage) msg).getMsg() + " left channel";
            }
            else if (msg.getType() == Message.MessageType.CHAT_MSG) {
                chatMsg = ((TextMessage) msg).getMsg();
            }
            else {
                return;
            }

            String msgs = output.getText().toString();
            if (!msgs.isEmpty()) {
                msgs += '\n';
            }
            msgs += chatMsg;

            output.setText(msgs);
            outputScroll.setScrollPercentY(1);
        }

        @Override
        public Collection<Message.MessageType> getMessageTypes() {
            return Arrays.asList(Message.MessageType.JOIN_SERVER,
                    Message.MessageType.CHAT_MSG, Message.MessageType.DISCONNECT);
        }
    }

    private final ServerConnection serverConnection;
    private final Label output;
    private final ScrollPane outputScroll;
    private final TextField input;

    public ChatHandler(ServerConnection serverConnection, Label chatMessages, ScrollPane scrollPane, TextField textfield) {
        this.serverConnection = serverConnection;
        this.output = chatMessages;
        this.outputScroll = scrollPane;
        this.input = textfield;
        this.consumer = new ChatMessageConsumer(serverConnection.getDelegator());
        this.listener = new ChatMessageListener();
        input.setTextFieldListener(listener);
    }

    public void processArrivedMessages() {
        consumer.consumeStoredMessages();
    }
}
