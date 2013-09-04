package com.rasanenj.warp.chat;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.rasanenj.warp.messaging.*;

/**
 * @author gilead
 */
public class ChatHandler implements MessageConsumer, TextField.TextFieldListener {
    private final ServerConnection serverConnection;
    private final Label output;
    private final ScrollPane outputScroll;
    private final TextField input;

    public ChatHandler(ServerConnection serverConnection, Label chatMessages, ScrollPane scrollPane, TextField textfield) {
        this.serverConnection = serverConnection;
        this.output = chatMessages;
        this.outputScroll = scrollPane;
        this.input = textfield;

        input.setTextFieldListener(this);
    }

    @Override
    public void consume(Player player, Message msg) {
        String chatMsg;
        if (msg.getType() == Message.MessageType.JOIN_SERVER) {
            chatMsg = ((JoinServerMessage) msg).getMsg() + " joined channel";
        }
        else if (msg.getType() == Message.MessageType.CHAT_MSG) {
            chatMsg = ((ChatMessage) msg).getMsg();
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
    public void register(MessageDelegator delegator) {
        delegator.register(this, Message.MessageType.JOIN_SERVER);
        delegator.register(this, Message.MessageType.CHAT_MSG);
    }

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
