package com.rasanenj.warp.messaging;

import com.sksamuel.gwt.websockets.Base64Utils;
import com.sksamuel.gwt.websockets.Websocket;
import com.sksamuel.gwt.websockets.WebsocketListener;

import java.nio.ByteBuffer;

/**
 * @author gilead
 */
public class ServerConnection implements WebsocketListener {
    final Websocket socket;
    private final MessageDelegator delegator;

    public ServerConnection(String host, MessageDelegator delegator) {
        this.delegator = delegator;
        socket = new Websocket("ws://localhost:8887");
        socket.addListener(this);
        socket.open();
    }

    public void send(Message message) {
        socket.send(message.encode());
    }

    @Override
    public void onClose() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onMessage(String msg) {
        byte[] bytes = Base64Utils.fromBase64(msg);
        Message message = MessageFactory.decode(ByteBuffer.wrap(bytes));
        delegator.delegate(null, message);
    }

    @Override
    public void onOpen() {
        send(new JoinServerMessage("gilead"));
    }
}
