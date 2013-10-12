package com.rasanenj.warp;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.rasanenj.warp.entities.ServerShip;
import com.rasanenj.warp.messaging.*;
import com.rasanenj.warp.tasks.IntervalTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class BattleServer extends IntervalTask {
    private final BattleMsgConsumer consumer;
    private final Vector2 pos = new Vector2();

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
                    serverPlayer.send(new CreateShipMessage(ship.getId(), ship.getWidth(), ship.getHeight(), ship.getMass(), ship.getInertia(),
                            ship.getMaxLinearForceForward(), ship.getMaxLinearForceBackward(), ship.getMaxLinearForceLeft(), ship.getMaxLinearForceRight(),
                            ship.getMaxHealth(), ship.getMaxVelocity(), ship.getMaxAngularVelocity()));
                }
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
                //b.applyTorque(message.getAngular(), true);
                b.applyForceToCenter(message.getX(), message.getY(), true);
                // log("speed:" + b.getLinearVelocity().len());
                // log("angular velocity:" + b.getAngularVelocity());
                // log(message.getX() +"," + message.getY());
                //b.applyLinearImpulse(message.getX(), message.getY(),
                //        ship.getEngineLocation().x, ship.getEngineLocation().y, true);
            }
            else if (msg.getType() == Message.MessageType.SHIP_STATS) {
                ShipStatsMessage message = (ShipStatsMessage) msg;

                log(message.getMaxHealth());

                ServerPlayer serverPlayer = (ServerPlayer) player;

                // add a new ship for the new player
                ServerShip ship = new ServerShip(world, 400f, 400f, 0, 1f, 0.4f, serverPlayer, message.getAcceleration(),
                        message.getMaxHealth(), message.getMaxSpeed(), message.getTurnSpeed());
                battleLoop.addShip(ship);
                // notify everyone about the new ship
                sendToAll(new CreateShipMessage(ship.getId(), ship.getWidth(), ship.getHeight(), ship.getMass(), ship.getInertia(),
                        ship.getMaxLinearForceForward(), ship.getMaxLinearForceBackward(), ship.getMaxLinearForceLeft(), ship.getMaxLinearForceRight(),
                        ship.getMaxHealth(), ship.getMaxVelocity(), ship.getMaxAngularVelocity()));
            }
        }

        @Override
        public Collection<Message.MessageType> getMessageTypes() {
            return Arrays.asList(Message.MessageType.JOIN_SERVER,
                    Message.MessageType.SET_ACCELERATION,
                    Message.MessageType.DISCONNECT,
                    Message.MessageType.SHIP_STATS);
        }
    }

    private static final float MESSAGES_IN_SECOND = 60;
    private final BattleLoop battleLoop;
    private final WSServer wsServer;
    private final World world;

    public BattleServer(BattleLoop battleLoop, WSServer wsServer, MessageDelegator delegator, World world) {
        super(MESSAGES_IN_SECOND);
        this.battleLoop = battleLoop;
        this.wsServer = wsServer;
        this.world = world;
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
        // log("Sending ship updates");
        ArrayList<ServerShip> ships = battleLoop.getShips();
        ArrayList<Message> messages = new ArrayList<Message>(ships.size());
        final float lerp1 = battleLoop.getRelativePhysicsTimeLeft();
        final float lerp2 = 1f - lerp1;
        for (ServerShip ship : ships) {
            ship.getInterpolatedPosition(pos, lerp1, lerp2);
            float angle = ship.getInterpolatedAngle(lerp1, lerp2);
            messages.add(new ShipPhysicsMessage(ship.getId(),
                    pos,
                    angle,
                    ship.getBody().getLinearVelocity(),
                    ship.getBody().getAngularVelocity()
            ));
        }
        sendToAll(messages);
    }
}
