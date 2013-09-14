package com.rasanenj.warp;

import com.rasanenj.warp.entities.ServerShip;
import com.rasanenj.warp.messaging.*;
import com.rasanenj.warp.tasks.Task;

import java.util.ArrayList;
import java.util.Collection;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class BattleServer extends Task implements MessageConsumer {
    private static final float MESSAGES_IN_SECOND = 4f;
    private final BattleLoop battleLoop;
    private final WSServer wsServer;

    public BattleServer(BattleLoop battleLoop, WSServer wsServer) {
        super(MESSAGES_IN_SECOND);
        this.battleLoop = battleLoop;
        this.wsServer = wsServer;
    }

    @Override
    public void consume(Player player, Message msg) {
        if (msg.getType() == Message.MessageType.JOIN_SERVER) {
            // TODO: send the player StartBattleMessage
            battleLoop.addPlayer(player);
            ServerPlayer serverPlayer = (ServerPlayer) player;

            // notify the new player about existing ships
            for (ServerShip ship : battleLoop.getShips()) {
                serverPlayer.send(new CreateShipMessage(ship));
            }

            // add a new ship for the new player
            ServerShip ship = new ServerShip(200f, 100f, serverPlayer);
            battleLoop.addShip(ship);

            // notify everyone about the new ship
            sendToAll(new CreateShipMessage(ship));
        }
        else if (msg.getType() == Message.MessageType.DISCONNECT) {
            battleLoop.removePlayer(player);
        }
        else if (msg.getType() == Message.MessageType.SET_ACCELERATION) {
            // TODO
        }
    }

    @Override
    public void register(MessageDelegator delegator) {
        delegator.register(this, Message.MessageType.JOIN_SERVER);
        delegator.register(this, Message.MessageType.SET_ACCELERATION);
        delegator.register(this, Message.MessageType.DISCONNECT);
    }

    private void sendToAll(Message msg) {
        for (Player player : battleLoop.getPlayers()) {
            ServerPlayer serverPlayer = (ServerPlayer) player; // TODO get rid of this once messages work better
            serverPlayer.send(msg);
        }
    }

    private void sendToAll(Collection<Message> messages) {
        for (Player player : battleLoop.getPlayers()) {
            ServerPlayer serverPlayer = (ServerPlayer) player; // TODO get rid of this once messages work better
            for (Message msg : messages) {
                serverPlayer.send(msg);
            }
        }
    }

    @Override
    protected void run() {
        // log("Sending ship updates");
        ArrayList<ServerShip> ships = battleLoop.getShips();
        ArrayList<Message> messages = new ArrayList<Message>(ships.size());
        for (ServerShip ship : ships) {
            ship.setX(ship.getX() + 1.05f);
            messages.add(new ShipPhysicsMessage(ship));
        }
        sendToAll(messages);
    }
}
