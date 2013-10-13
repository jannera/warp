package com.rasanenj.warp.messaging;

import com.badlogic.gdx.utils.Array;
import com.rasanenj.warp.FleetStatsFetcher;
import com.sksamuel.gwt.websockets.Base64Utils;
import com.sksamuel.gwt.websockets.Websocket;
import com.sksamuel.gwt.websockets.WebsocketListener;

import java.nio.ByteBuffer;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class ServerConnection implements WebsocketListener {
    final Websocket socket;
    private final MessageDelegator delegator;
    private final FleetStatsFetcher statsFetcher;

    public ServerConnection(String host, MessageDelegator delegator) {
        this.delegator = delegator;
        socket = new Websocket(host);
        socket.addListener(this);
        this.statsFetcher = new FleetStatsFetcher(2);
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
        send(new JoinServerMessage("gilead", -1, -1));
        statsFetcher.loadJSON(this);
    }

    public void open() {
        socket.open();
    }

    public MessageDelegator getDelegator() {
        return delegator;
    }

    public void sendShipStats(Array<ShipStatsMessage> msgs) {
        for (ShipStatsMessage m : msgs) {
            send(m);
        }
    }
}
