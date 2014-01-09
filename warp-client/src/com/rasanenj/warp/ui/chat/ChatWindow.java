package com.rasanenj.warp.ui.chat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.rasanenj.warp.Assets;
import com.rasanenj.warp.chat.ChatHandler;
import com.rasanenj.warp.chat.MessageListener;

/**
 * @author gilead
 */
public class ChatWindow implements MessageListener {
    final Window window;
    private final Label output;
    private final ScrollPane outputScroll;
    private final TextField input;
    private ChatHandler chatHandler;
    private final ChatMessageListener listener;

    public ChatWindow() {
        this.listener = new ChatMessageListener();

        input = new TextField("", Assets.skin);
        input.setMessageText("Write your messages here");

        output = new Label("", Assets.skin);
        output.setHeight(30);
        output.setFillParent(true);
        output.setWrap(true);

        outputScroll = new ScrollPane(output, Assets.skin);

        final int rows = 10;
        window = new Window("Chat", Assets.skin);
        // window.debug();
        window.defaults().spaceBottom(10);
        window.row().fill().expandX();
        window.add(outputScroll).minWidth(300).minHeight(output.getHeight() * rows).expand().fill().colspan(2);
        window.row();
        window.add(input).minWidth(100).expandX().fillX().colspan(3);
        window.pack();

        input.setTextFieldListener(listener);
    }

    private class ChatMessageListener implements TextField.TextFieldListener {
        @Override
        public void keyTyped(TextField textField, char key) {
            if (key == '\n' || key == '\r') {
                String msg = textField.getText();
                if (msg.isEmpty()) {
                    return;
                }
                textField.setText("");
                chatHandler.send(msg);
            }
        }
    }

    @Override
    public void handle(String message) {
        String msgs = output.getText().toString();
        if (!msgs.isEmpty()) {
            msgs += '\n';
        }
        msgs += message;

        output.setText(msgs);
        outputScroll.setScrollPercentY(1);
    }

    public Window getWindow() {
        return window;
    }

    public void setChatHandler(ChatHandler chatHandler) {
        this.chatHandler = chatHandler;
    }
}
