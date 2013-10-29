package com.rasanenj.warp;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.rasanenj.warp.entities.ClientShip;
import com.rasanenj.warp.entities.ShipStats;
import com.rasanenj.warp.messaging.*;
import com.rasanenj.warp.screens.BattleScreen;
import com.rasanenj.warp.systems.ShipShooting;
import com.rasanenj.warp.systems.ShipSteering;
import com.rasanenj.warp.tasks.MoveCameraTask;
import com.rasanenj.warp.tasks.TaskHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;

import static com.rasanenj.warp.Log.log;

/**
 * Handles UI and messaging to and from server.
 *
 * @author gilead
 */
public class BattleHandler {
    private final BattleMessageConsumer consumer;
    private final ShipClickListener shipClickListener;
    private final TaskHandler taskHandler;
    private final MoveCameraTask moveCameraTask;
    private static final Color[] playerColors = {new Color(0.5f, 0, 0, 1), new Color(0, 0.5f, 0, 1), new Color(0, 0, 0.5f, 1), new Color(0, 0.5f, 0.5f, 1)};
    private static final Color[] hiliteColors = {new Color(1f, 0, 0, 1), new Color(0, 1, 0, 1), new Color(0,0,1,1), new Color(0, 1, 1, 1)};
    private final Array<Player> players = new Array<Player>(true, 1);
    private final ShipHover hoverOverShipListener;
    private final Image targetImage;
    private final ShipShooting shipShooting;
    private ClientShip hoveringOverTarget;
    private long myId = -1;
    private final FleetStatsFetcher statsFetcher;

    public BattleHandler(BattleScreen screen, ServerConnection conn) {
        conn.register(new ConnectionListener());
        this.statsFetcher = new FleetStatsFetcher();
        this.screen = screen;
        this.conn = conn;
        this.shipClickListener = new ShipClickListener();
        screen.getStage().addListener(new StageListener());
        shipSteering = new ShipSteering(ships, conn);
        shipShooting = new ShipShooting(ships, conn);
        this.consumer = new BattleMessageConsumer(conn.getDelegator());
        this.taskHandler = new TaskHandler();
        this.moveCameraTask = new MoveCameraTask(screen);
        this.hoverOverShipListener = new ShipHover();
        taskHandler.addToTaskList(moveCameraTask);
        this.targetImage = new Image(Assets.aimingTargetTexture);
        this.targetImage.setVisible(false);
        this.targetImage.setBounds(0, 0, 1, 1);
        this.targetImage.setZIndex(ZOrder.firingTarget.ordinal());
        screen.getStage().addActor(targetImage);
    }

    private class BattleMessageConsumer extends MessageConsumer {
        public BattleMessageConsumer(MessageDelegator delegator) {
            super(delegator);
        }

        private boolean firstPosSet = false;

        @Override
        public void consume(Player player, Message msg) {
            if (msg.getType() == Message.MessageType.UPDATE_SHIP_PHYSICS) {
                ShipPhysicsMessage shipPhysicsMessage = (ShipPhysicsMessage) msg;
                ClientShip ship = getShip(shipPhysicsMessage.getId());
                if (ship == null) {
                    log("Couldn't find a ship with id " + shipPhysicsMessage.getId());
                }
                else {
                    long updateTime = shipPhysicsMessage.getTimestamp();
                    ship.setPosition(shipPhysicsMessage.getX(), shipPhysicsMessage.getY());
                    ship.setRotation(shipPhysicsMessage.getAngle());
                    ship.setVelocity(shipPhysicsMessage.getVelX(), shipPhysicsMessage.getVelY(), shipPhysicsMessage.getAngularVelocity(), updateTime);
                    ship.setUpdateTime(updateTime);
                    if (!firstPosSet && ship.getOwner().getId() == myId) {
                        ship.getCenterPos(tmp);
                        screen.setCameraPos(tmp.x, tmp.y);
                        firstPosSet = true;
                    }
                }
            }
            else if (msg.getType() == Message.MessageType.CREATE_SHIP) {
                CreateShipMessage message = (CreateShipMessage) msg;
                Player owningPlayer = getPlayer(message.getOwnerId());

                ClientShip ship = new ClientShip(message.getId(), owningPlayer, message.getWidth(),
                        message.getHeight(), message.getStats());
                ships.add(ship);
                screen.getStage().addActor(ship);
                ship.attach(screen.getStage());
                ship.addListener(shipClickListener);
                ship.addListener(hoverOverShipListener);
                ship.setZIndex(ZOrder.ship.ordinal());


                if (owningPlayer == null) {
                    log(Level.SEVERE, "Couldn't find player with id: " + message.getOwnerId());
                }
                else {
                    Color c = getBasicColor(owningPlayer);
                    ship.setColor(c);
                }
            }

            else if (msg.getType() == Message.MessageType.JOIN_SERVER) {
                JoinServerMessage message = (JoinServerMessage) msg;
                if (message.getId() != -1) {
                    // terrible hack in order to use JoinServerMessages in both battle and chat
                    Player p = new Player(message.getPlayerName(), message.getId(), message.getColorIndex());
                    log("joined in battle:" + p);
                    if (myId == -1) {
                        myId = message.getId();
                    }
                    players.add(p);
                }
            }

            else if (msg.getType() == Message.MessageType.SHOOT_DAMAGE) {
                ShootDamageMessage message = (ShootDamageMessage) msg;
                ClientShip target = getShip(message.getTarget());
                if (target == null) {
                    log("Couldn't find target with id " + message.getTarget());
                }
                else {
                    target.reduceHealth(message.getDamage());
                    screen.addDamageText(target, message.getDamage());
                }
            }

            else if (msg.getType() == Message.MessageType.SHIP_DESTRUCTION) {
                ShipDestructionMessage message = (ShipDestructionMessage) msg;
                long id = message.getId();

                ClientShip removedShip = removeShip(id);
                if (removedShip == null)
                {
                    log(Level.SEVERE, "Couldn't remove ship with id " + id);
                }
                else {
                    screen.getStage().getRoot().removeActor(removedShip);
                }

                // tell all shooting ships to stop shooting
                for (ClientShip ship : ships) {
                    if (ship.getFiringTarget() == removedShip) {
                        ship.setFiringTarget(null);
                    }
                }


            }
        }

        @Override
        public Collection<Message.MessageType> getMessageTypes() {
            return Arrays.asList(Message.MessageType.UPDATE_SHIP_PHYSICS,
                    Message.MessageType.CREATE_SHIP,
                    Message.MessageType.JOIN_SERVER,
                    Message.MessageType.SHOOT_DAMAGE,
                    Message.MessageType.SHIP_DESTRUCTION);
        }
    }

    private class ShipHover extends InputListener {
        @Override
        public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor) {
            ClientShip ship = (ClientShip) event.getTarget();

            if (ship.getOwner().getId() == myId) {
                return;
            }

            log("cursor entered " + event.getTarget());

            hoveringOverTarget = ship;
            targetImage.setVisible(true);
            updateTargetImagePos();
        }

        @Override
        public void exit (InputEvent event, float x, float y, int pointer, Actor toActor) {
            log("cursor exited " + event.getTarget());
            hoveringOverTarget = null;
            targetImage.setVisible(false);
        }
    }

    private class ShipClickListener extends InputListener {

        @Override
        public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
            return true;
        }

        @Override
        public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
            log("clicked Ship" + event.getTarget() + " @ (" + x + ", " + y + ")");

            ClientShip clientShip = (ClientShip) event.getTarget();

            if (clientShip.getOwner().getId() == myId) {
                // clicked friendly ship, so select it
                if (selectedShip != null) {
                    selectedShip.setColor(getBasicColor(selectedShip.getOwner()));
                }
                selectedShip = (ClientShip) event.getTarget();
                selectedShip.clearTargetPos();
                selectedShip.setColor(getHiliteColor(selectedShip.getOwner()));
            }
            else {
                // clicked non-friendly ship, so kill it
                if (selectedShip != null) {
                    selectedShip.setFiringTarget(clientShip);
                    log (selectedShip + " shooting " + clientShip);
                }
            }
            event.handle();


        }
    }

    private Color getHiliteColor(Player player) {
        return hiliteColors[player.getColorIndex()];
    }

    private enum DragState {
        NOT_DRAGGING, STARTING, DRAGGING
    }

    private class StageListener extends InputListener {
        DragState dragState;

        private final Vector2 startPoint = new Vector2();

        public StageListener() {
            startPoint.set(screen.getStage().getCamera().position.x, screen.getStage().getCamera().position.y);
        }

        public void touchDragged (InputEvent event, float x, float y, int pointer) {
            if (dragState == DragState.NOT_DRAGGING) {
                return;
            }

            dragState = DragState.DRAGGING;

            float dx = startPoint.x - x;
            float dy = startPoint.y - y;
            if (dx != 0 && dy != 0) {
                moveCameraTask.setTarget(dx, dy);
            }
            log(x + ", " + y + " -> " + dx + ", " + dy);
            // screen.translateCamera(dx, dy);
            startPoint.set(x, y);
        }
        public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
            if (event.getTarget() instanceof ClientShip) {
                return false;
            }
            dragState = DragState.STARTING;
            startPoint.set(x, y);
            return true;
        }

        public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
            if (dragState == DragState.DRAGGING) {
                event.cancel();
                dragState = DragState.NOT_DRAGGING;
            }
            else {
                if (selectedShip != null) {
                    selectedShip.setTargetPos(x, y);
                    screen.setCameraPos(x, y);
                    event.stop();
                }
            }
        }

        @Override
        public boolean scrolled(InputEvent event, float x, float y, int amount) {
            screen.zoom(0.1f * amount, x, y);
            return true;
        }
    }

    private final ServerConnection conn;
    private final BattleScreen screen;
    private final ShipSteering shipSteering;

    private final ArrayList<ClientShip> ships = new ArrayList<ClientShip>();

    private ClientShip selectedShip = null;

    private ClientShip getShip(long id) {
        for (ClientShip ship : ships) {
            if (ship.getId() == id) {
                return ship;
            }
        }
        return null;
    }

    private ClientShip removeShip(long id) {
        ClientShip tgt = null;
        for (ClientShip ship : ships) {
            if (ship.getId() == id) {
                tgt = ship;
            }
        }
        if (tgt != null) {
            ships.remove(tgt);
            return tgt;
        }
        return null;
    }

    public void update(float delta) {
        long timeNow = System.currentTimeMillis();
        for (ClientShip s : ships) {
            // s.updatePos(timeNow);
        }
        consumer.consumeStoredMessages();
        shipSteering.update();
        shipShooting.update();
        taskHandler.update(delta);
        updateTargetImagePos();
    }

    final Vector2 tmp = new Vector2();
    private void updateTargetImagePos() {
        if (hoveringOverTarget == null) {
            return;
        }
        // align the center of the targeting image with the center of the ship
        hoveringOverTarget.getCenterPos(tmp);
        tmp.sub(targetImage.getWidth() / 2f, targetImage.getHeight() / 2f);
        targetImage.setPosition(tmp.x, tmp.y);
    }

    public ArrayList<ClientShip> getShips() {
        return ships;
    }

    public Player getPlayer(long id) {
        for (Player p : players) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    private Color getBasicColor(Player player) {
        return playerColors[player.getColorIndex()];
    }

    public ClientShip getSelectedShip() {
        return selectedShip;
    }

    private class ConnectionListener implements ServerConnection.OpenCloseListener {
        private class StatsToFleet implements FleetStatsFetcher.StatsReceiver {

            @Override
            public void receive(Array<ShipStats> stats) {
                for(ShipStats s : stats) {
                    conn.send(new ShipStatsMessage(s));
                }
            }
        }

        @Override
        public void onOpen() {
            conn.send(new JoinServerMessage("gilead", -1, -1));

            StatsToFleet transformer = new StatsToFleet();

            if (Settings.OFFLINE_MODE) {
                transformer.receive(statsFetcher.parse(Constants.OFFLINE_FLEET));
            }
            else {
                statsFetcher.loadJSON(transformer);
            }
        }

        @Override
        public void onClose() {
            // TODO: maybe tell the screen the connection was lost?
        }
    }
}
