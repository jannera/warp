package com.rasanenj.warp;

import com.rasanenj.warp.messaging.*;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class WSServer extends WebSocketServer {

    private final MessageDelegator delegator;
    private final Collection<ServerPlayer> players;

    public WSServer( int port, MessageDelegator delegator) throws UnknownHostException {
        super( new InetSocketAddress( port ) );
        this.delegator = delegator;
        players = new ArrayList<ServerPlayer>();
        // WebSocketImpl.DEBUG = true;
    }

    public WSServer( InetSocketAddress address, MessageDelegator delegator ) {
        super( address );
        this.delegator = delegator;
        players = new ArrayList<ServerPlayer>();
        // WebSocketImpl.DEBUG = true;
    }

    public Collection<ServerPlayer> getPlayers() {
        return players;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        log( conn.getRemoteSocketAddress().getAddress().getHostAddress() + " connected server!" );
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Player player = getPlayer(conn);
        log(player.getName() + " disconnected");
        remove(player);
        DisconnectMessage msg = new DisconnectMessage(player);
        sendToAll(msg);
        delegator.delegate(player, msg);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        byte[] bytes = Base64Utils.fromBase64(message);
        onMessage(conn, ByteBuffer.wrap(bytes));
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        // TODO some error handling :p
    }

    @Override
    public void onMessage( WebSocket conn, ByteBuffer message ) {
        Message msg = MessageFactory.decode(message);

        ServerPlayer player;
        if (msg.getType() == Message.MessageType.JOIN_SERVER) {
            JoinServerMessage joinServerMsg = (JoinServerMessage)msg;
            player = new ServerPlayer(conn, joinServerMsg.getPlayerName());
            log(player.getName() + " (" + player.getId() + ") joined server");
            players.add(player);
        }
        else {
            player = getPlayer(conn);
        }
        delegator.delegate(player, msg);
    }

    private ServerPlayer getPlayer(WebSocket conn) {
        // TODO: replace this with a concurrent hashmap
        for(ServerPlayer player : players) {
            if (player.getConn() == conn) {
                return player;
            }
        }
        return null;
    }

    public void sendToAll(Message message) {
        for (ServerPlayer player : players) {
            player.send(message);
        }
    }

    private void remove(Player player) {
        players.remove(player);
    }
}
