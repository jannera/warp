package com.rasanenj.warp;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.rasanenj.warp.entities.ServerShip;
import com.rasanenj.warp.messaging.*;
import com.rasanenj.warp.tasks.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class BattleServer extends Task {
    private final BattleMsgConsumer consumer;

    private class BattleMsgConsumer extends MessageConsumer {
        public BattleMsgConsumer(MessageDelegator delegator) {
            super(delegator);
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
                ServerShip ship = new ServerShip(world, 400f, 400f, 0, 0.1f, 0.2f, serverPlayer);
                battleLoop.addShip(ship);
                // notify everyone about the new ship
                sendToAll(new CreateShipMessage(ship));
            }
            else if (msg.getType() == Message.MessageType.DISCONNECT) {
                battleLoop.removePlayer(player);
                battleLoop.removeAllShips(player);
            }
            else if (msg.getType() == Message.MessageType.SET_ACCELERATION) {
                AccelerationMessage message = (AccelerationMessage) msg;
                ServerShip ship = battleLoop.getShip(message.getId());

                if (ship == null) {
                    log(Level.SEVERE, "Could not find ship with id " + message.getId());
                    return;
                }


                Body b = ship.getBody();
                b.applyAngularImpulse(message.getAngular(), true);
                b.applyForceToCenter(message.getX(), message.getY(), true);
                log("speed:" + b.getLinearVelocity().len());
                // log(message.getX() +"," + message.getY());
                //b.applyLinearImpulse(message.getX(), message.getY(),
                //        ship.getEngineLocation().x, ship.getEngineLocation().y, true);
            }
        }

        @Override
        public Collection<Message.MessageType> getMessageTypes() {
            return Arrays.asList(Message.MessageType.JOIN_SERVER,
                    Message.MessageType.SET_ACCELERATION,
                    Message.MessageType.DISCONNECT);
        }
    }

    private static final float MESSAGES_IN_SECOND = 4f;
    private final BattleLoop battleLoop;
    private final WSServer wsServer;
    private final World world;

    public BattleServer(BattleLoop battleLoop, WSServer wsServer, MessageDelegator delegator) {
        super(MESSAGES_IN_SECOND);
        this.battleLoop = battleLoop;
        this.wsServer = wsServer;
        GdxNativesLoader.load();
        this.world = new World(new Vector2(0,0), true);
        this.consumer = new BattleMsgConsumer(delegator);
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
        consumer.consumeStoredMessages();
        world.step(1/MESSAGES_IN_SECOND, 0, 0);
        // log("Sending ship updates");
        ArrayList<ServerShip> ships = battleLoop.getShips();
        ArrayList<Message> messages = new ArrayList<Message>(ships.size());
        for (ServerShip ship : ships) {
            // ship.setX(ship.getX() + 1.05f);
            messages.add(new ShipPhysicsMessage(ship.getId(), ship.getBody()));
        }
        sendToAll(messages);
    }
}
