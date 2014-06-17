package com.rasanenj.warp;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.rasanenj.warp.entities.ServerShip;
import com.rasanenj.warp.messaging.*;
import com.rasanenj.warp.tasks.IntervalTask;

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
    private static final float[] shipOffsets = new float[8];

    static {
        for (int i=0; i < shipOffsets.length; i++) {
            shipOffsets[i] = 0;
        }
    }

    private final ShipPhysicsUpdate physicsUpdate;

    private class BattleMsgConsumer extends MessageConsumer {
        public BattleMsgConsumer(MessageDelegator delegator) {
            super(delegator);
        }

        @Override
        public void consume(Player player, Message msg) {
            if (msg.getType() == Message.MessageType.JOIN_BATTLE) {
                ServerPlayer serverPlayer = (ServerPlayer) player;

                shipOffsets[serverPlayer.getColorIndex()] = 0;

                // notify the player about himself
                serverPlayer.send(new JoinBattleMessage(serverPlayer.getName(), serverPlayer.getId(), serverPlayer.getColorIndex()));

                // notify the new player about existing players
                for (Player p : battleLoop.getPlayers()) {
                    // log("notifying " + serverPlayer + " about " + p);
                    serverPlayer.send(new JoinBattleMessage(p.getName(), p.getId(), p.getColorIndex()));
                }

                // notify existing players about the new player
                for (Player p : battleLoop.getPlayers()) {
                    // log("notifying " + p + " about " + serverPlayer);
                    ((ServerPlayer) p).send(new JoinBattleMessage(player.getName(), player.getId(), player.getColorIndex()));
                }

                battleLoop.addPlayer(player);

                final float lerp1 = battleLoop.getRelativePhysicsTimeLeft();
                final float lerp2 = 1f - lerp1;

                // notify the new player about existing ships
                for (ServerShip ship : battleLoop.getShips()) {
                    // log ("player id was " + ship.getPlayer().getId());
                    serverPlayer.send(new CreateShipMessage(ship.getId(), ship.getPlayer().getId(), ship.getStats()));
                    ship.getInterpolatedPosition(pos, lerp1, lerp2);
                    float angle = ship.getInterpolatedAngle(lerp1, lerp2);
                    serverPlayer.send(new ShipPhysicsMessage(ship.getId(), pos, angle, ship.getBody(), true));
                }
            }
            else if (msg.getType() == Message.MessageType.DISCONNECT) {
                battleLoop.removePlayer(player);
                Array<ServerShip> ships = battleLoop.getShipsOwnedByPlayer(player);
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
                float shipHeight = message.getStats().getHeight();

                shipOffsets[serverPlayer.getColorIndex()] += shipHeight * 2;

                ServerShip ship = new ServerShip(world, position.x,
                        position.y + shipOffsets[serverPlayer.getColorIndex()], 0,
                        serverPlayer, message.getStats());
                shipOffsets[serverPlayer.getColorIndex()] += shipHeight * 2;
                battleLoop.addShip(ship);
                // notify everyone about the new ship
                sendToAll(new CreateShipMessage(ship.getId(), ship.getPlayer().getId(), ship.getStats()));
                final float lerp1 = battleLoop.getRelativePhysicsTimeLeft();
                final float lerp2 = 1f - lerp1;
                ship.getInterpolatedPosition(pos, lerp1, lerp2);
                float angle = ship.getInterpolatedAngle(lerp1, lerp2);
                serverPlayer.send(new ShipPhysicsMessage(ship.getId(), pos, angle, ship.getBody(), true));
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
                    Body shooterBody = shooter.getBody();
                    Body targetBody = target.getBody();
                    float dmg = damage.getDamage(shooterBody.getWorldCenter(), targetBody.getWorldCenter(),
                            shooterBody.getLinearVelocity(), targetBody.getLinearVelocity(),
                            shooter.getStats(), target.getStats());
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
            return Arrays.asList(Message.MessageType.JOIN_BATTLE,
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
        this.physicsUpdate = new ShipPhysicsUpdate();
    }

    private void sendToAll(Message msg) {
        for (Player player : battleLoop.getPlayers()) {
            ServerPlayer serverPlayer = (ServerPlayer) player; // TODO get rid of this once messages work better
            serverPlayer.send(msg);
        }
    }

    private void sendToAll(Array<Message> messages) {
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
        physicsUpdate.update();
    }

    private class ShipPhysicsUpdate extends IntervalTask {
        private static final float PHYSICS_UPPDATES_IN_SECOND = 30f;

        public ShipPhysicsUpdate() {
            super(PHYSICS_UPPDATES_IN_SECOND);
        }

        @Override
        protected void run() {
            Array<ServerShip> ships = battleLoop.getShips();
            Array<Message> messages = new Array<Message>(ships.size);
            final float lerp1 = battleLoop.getRelativePhysicsTimeLeft();
            final float lerp2 = 1f - lerp1;
            // TODO: there's no reason to create and populate a list on every update
            // TODO: we could just as well reuse old messages
            for (ServerShip ship : ships) {
                ship.getInterpolatedPosition(pos, lerp1, lerp2);
                float angle = ship.getInterpolatedAngle(lerp1, lerp2);
                messages.add(new ShipPhysicsMessage(ship.getId(), pos, angle, ship.getBody(), false));
            }
            sendToAll(messages);
        }
    }
}
