package com.rasanenj.warp.messaging;

import com.badlogic.gdx.utils.Array;
import com.sksamuel.gwt.websockets.Base64Utils;
import com.sksamuel.gwt.websockets.Websocket;
import com.sksamuel.gwt.websockets.WebsocketListener;

import java.nio.ByteBuffer;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class ServerConnection implements WebsocketListener {
    public interface OpenCloseListener {
        public abstract void onOpen();

        public abstract void onClose();
    }
    final Websocket socket;
    private final MessageDelegator delegator;

    private final Array<OpenCloseListener> listeners =
            new Array<OpenCloseListener>(false, 1);

    public ServerConnection(String host, MessageDelegator delegator) {
        this.delegator = delegator;
        socket = new Websocket(host);
        socket.addListener(this);
    }

    public void register(OpenCloseListener l) {
        listeners.add(l);
    }

    public void send(Message message) {
        socket.send(message.encode());
    }

    @Override
    public void onClose() {
        for(OpenCloseListener l : listeners) {
            l.onClose();
        }
    }

    @Override
    public void onMessage(String msg) {
        byte[] bytes = Base64Utils.fromBase64(msg);
        Message message = MessageFactory.decode(ByteBuffer.wrap(bytes));
        delegator.delegate(null, message);
    }

    @Override
    public void onOpen() {
        for(OpenCloseListener l : listeners) {
            l.onOpen();
        }
    }

    public void open() {
        socket.open();
    }

    public MessageDelegator getDelegator() {
        return delegator;
    }
}
