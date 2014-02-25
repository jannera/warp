package com.rasanenj.warp.messaging;

import com.badlogic.gdx.utils.Array;
import com.rasanenj.warp.Settings;
import com.sksamuel.gwt.websockets.Base64Utils;
import com.sksamuel.gwt.websockets.Websocket;
import com.sksamuel.gwt.websockets.WebsocketListener;
import com.sksamuel.gwt.websockets.WebsocketRealBinary;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class ServerConnection implements WebsocketListener {
    private long lastSent = MessageStats.UNKNOWN; // the last time we sent a message, in my time
    private final MessageStats lastReceived = new MessageStats(); // the last received message stats

    public interface OpenCloseListener {
        public abstract void onOpen();

        public abstract void onClose();
    }
    final WebsocketRealBinary socket;
    private final MessageDelegator delegator;

    private final Array<OpenCloseListener> listeners =
            new Array<OpenCloseListener>(false, 1);

    public ServerConnection(String host, MessageDelegator delegator) {
        this.delegator = delegator;
        socket = new WebsocketRealBinary(host);
        socket.addListener(this);
    }

    public void register(OpenCloseListener l) {
        listeners.add(l);
    }

    public void send(Message message) {
        message.getLastMessageSenderReceivedStats().copyFrom(lastReceived);
        lastSent = message.getThisStats().getSent();
        if (lastSent == MessageStats.UNKNOWN) {
            lastSent = System.currentTimeMillis();
            message.getThisStats().setSent(lastSent);
            // some messages want to set this by hand, others do not
        }

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
        onMessage(bytes);
    }

    @Override
    public void onOpen() {
        for(OpenCloseListener l : listeners) {
            l.onOpen();
        }
    }

    private static final long LATENCY_LOGGING_INTERVAL_MS = 5000;
    private long lastLatencyPrint = 0;

    @Override
    public void onMessage(byte[] arr) {
        Message message = MessageFactory.decode(ByteBuffer.wrap(arr));
        lastReceived.copyFrom(message.getThisStats());

        long timeNow = System.currentTimeMillis();
        if (timeNow - lastLatencyPrint > LATENCY_LOGGING_INTERVAL_MS && Settings.logLatency) {
            lastLatencyPrint = timeNow;
            log("estimated latency (half of roundtime) was " + message.estimateLatency());
        }

        delegator.delegate(null, message);
    }

    public void open() {
        socket.open();
    }

    public MessageDelegator getDelegator() {
        return delegator;
    }
}
