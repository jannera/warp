package com.rasanenj.warp;

import com.rasanenj.warp.messaging.Message;
import com.rasanenj.warp.messaging.MessageStats;
import com.rasanenj.warp.messaging.Player;
import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;

/**
 * @author gilead
 */
public class ServerPlayer extends Player {
    private final WebSocket conn;

    private long lastSent = MessageStats.UNKNOWN;         // the last time we sent a message, in my time
    private final MessageStats lastReceived = new MessageStats(); // the last received message stats

    public ServerPlayer(WebSocket conn, String name, int colorIndex) {
        super(name, colorIndex);
        this.conn = conn;
    }

    public void send(Message msg) {
        lastSent = msg.getThisStats().getSent();
        if (lastSent == MessageStats.UNKNOWN) {
            lastSent = System.currentTimeMillis();
            msg.getThisStats().setSent(lastSent);
            // some messages want to set this by hand, others do not
        }
        msg.getLastMessageSenderReceivedStats().copyFrom(lastReceived);

        try {
            if (!conn.isClosing()) {
                conn.send(msg.encode());
            }
        }
        catch (WebsocketNotConnectedException e) {
            e.printStackTrace();
        }
    }

    public WebSocket getConn() {
        return conn;
    }

    public MessageStats getLastReceived() {
        return lastReceived;
    }
}
