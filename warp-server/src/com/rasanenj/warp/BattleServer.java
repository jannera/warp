package com.rasanenj.warp;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
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
    private final DamageModeler damage = new DamageModeler();

    private final Vector2[] startingPositions = {new Vector2(400, 400), new Vector2(420, 400),
       new Vector2(400, 420), new Vector2(420, 420), new Vector2(440, 400), new Vector2(400, 440)};
    private static final int[] shipOffsetCounters = new int[8];

    static {
        for (int i=0; i < shipOffsetCounters.length; i++) {
            shipOffsetCounters[i] = 0;
        }
    }

    private static final float SHIP_HEIGHT = 0.4f, SHIP_WIDTH = 1f;

    private class BattleMsgConsumer extends MessageConsumer {
        public BattleMsgConsumer(MessageDelegator delegator) {
            super(delegator);
        }

        @Override
        public void consume(Player player, Message msg) {
            if (msg.getType() == Message.MessageType.JOIN_SERVER) {
                // TODO: send the player StartBattleMessage
                ServerPlayer serverPlayer = (ServerPlayer) player;

                shipOffsetCounters[serverPlayer.getColorIndex()] = 0;

                // notify the player about himself
                serverPlayer.send(new JoinServerMessage(serverPlayer.getName(), serverPlayer.getId(), serverPlayer.getColorIndex()));

                // notify the new player about existing players
                for (Player p : battleLoop.getPlayers()) {
                    // log("notifying " + serverPlayer + " about " + p);
                    serverPlayer.send(new JoinServerMessage(p.getName(), p.getId(), p.getColorIndex()));
                }

                // notify existing players about the new player
                for (Player p : battleLoop.getPlayers()) {
                    // log("notifying " + p + " about " + serverPlayer);
                    ((ServerPlayer) p).send(new JoinServerMessage(player.getName(), player.getId(), player.getColorIndex()));
                }

                battleLoop.addPlayer(player);

                // notify the new player about existing ships
                for (ServerShip ship : battleLoop.getShips()) {
                    // log ("player id was " + ship.getPlayer().getId());
                    serverPlayer.send(new CreateShipMessage(ship.getId(), ship.getPlayer().getId(), ship.getWidth(),
                            ship.getHeight(), ship.getStats()));
                }
            }
            else if (msg.getType() == Message.MessageType.DISCONNECT) {
                battleLoop.removePlayer(player);
                ArrayList<ServerShip> ships = battleLoop.getShipsOwnedByPlayer(player);
                for (ServerShip ship : ships) {
                    // notify all players still left in the game to remove the ships
                    // TODO: maybe in the future we want to do something else than remove players ships when he disconnects..
                    // TODO: maybe give some time to reconnect
                    sendToAll(new ShipDestructionMessage(ship.getId()));
                }
                battleLoop.removeAllShips(ships);
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
                Vector2 center = ship.getBody().getWorldCenter();
                b.applyLinearImpulse(message.getX(), message.getY(),
                        center.x, center.y, true);
            }
            else if (msg.getType() == Message.MessageType.SHIP_STATS) {
                ShipStatsMessage message = (ShipStatsMessage) msg;

                ServerPlayer serverPlayer = (ServerPlayer) player;

                // add a new ship based on the stats
                Vector2 position = startingPositions[serverPlayer.getColorIndex()];
                float yOffSet = shipOffsetCounters[serverPlayer.getColorIndex()] * SHIP_WIDTH * 5;
                shipOffsetCounters[serverPlayer.getColorIndex()]++;
                ServerShip ship = new ServerShip(world, position.x, position.y + yOffSet, 0, SHIP_WIDTH, SHIP_HEIGHT,
                        serverPlayer, message.getAcceleration(), message.getStats());
                battleLoop.addShip(ship);
                // notify everyone about the new ship
                sendToAll(new CreateShipMessage(ship.getId(), ship.getPlayer().getId(), ship.getWidth(), ship.getHeight(),
                        ship.getStats()));
            }
            else if (msg.getType() == Message.MessageType.SHOOT_REQUEST) {
                ShootRequestMessage message = (ShootRequestMessage) msg;
                ServerShip shooter = battleLoop.getShip(message.getId());
                ServerShip target = battleLoop.getShip(message.getTarget());
                if (shooter == null) {
                    log("Couldn't find shooter with id " + message.getId());
                }
                if (target == null) {
                    log("Couldnt find target with id " + message.getTarget());
                }
                if (shooter != null && target != null) {
                    float dmg = damage.getDamage(shooter, target);
                    sendToAll(new ShootDamageMessage(shooter.getId(), target.getId(), dmg));
                    target.reduceHealth(dmg);
                    if (target.getHealth() < 0) {
                        sendToAll(new ShipDestructionMessage(target.getId()));
                        battleLoop.removeShip(target.getId());
                    }
                }
            }
        }

        @Override
        public Collection<Message.MessageType> getMessageTypes() {
            return Arrays.asList(Message.MessageType.JOIN_SERVER,
                    Message.MessageType.SET_ACCELERATION,
                    Message.MessageType.DISCONNECT,
                    Message.MessageType.SHIP_STATS,
                    Message.MessageType.SHOOT_REQUEST);
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
