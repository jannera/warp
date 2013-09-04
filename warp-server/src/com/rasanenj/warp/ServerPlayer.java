package com.rasanenj.warp;

import com.rasanenj.warp.messaging.Message;
import com.rasanenj.warp.messaging.Player;
import org.java_websocket.WebSocket;

/**
 * @author gilead
 */
public class ServerPlayer extends Player {
    private final WebSocket conn;

    public ServerPlayer(WebSocket conn, String name) {
        super(name);
        this.conn = conn;
    }

    public void send(Message msg) {
        conn.send(Base64Utils.toBase64(msg.encode()));
    }

    public WebSocket getConn() {
        return conn;
    }
}
