package com.rasanenj.warp;

import com.rasanenj.warp.messaging.Message;
import com.rasanenj.warp.messaging.Player;
import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;

/**
 * @author gilead
 */
public class ServerPlayer extends Player {
    private final WebSocket conn;

    private static int colorIndexCounter = 0;

    public ServerPlayer(WebSocket conn, String name) {
        super(name, colorIndexCounter++);
        this.conn = conn;
    }



    public void send(Message msg) {
        try {
            conn.send(Base64Utils.toBase64(msg.encode()));
        }
        catch (WebsocketNotConnectedException e) {
            e.printStackTrace();
        }
    }

    public WebSocket getConn() {
        return conn;
    }
}
