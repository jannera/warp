package com.rasanenj.warp;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Array;
import com.rasanenj.warp.entities.ClientShip;
import com.rasanenj.warp.messaging.*;
import com.rasanenj.warp.screens.BattleScreen;
import com.rasanenj.warp.systems.ShipSteering;
import com.rasanenj.warp.tasks.MoveCameraTask;
import com.rasanenj.warp.tasks.TaskHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class BattleHandler {
    private final BattleMessageConsumer consumer;
    private final StageClickListener stageClickListener;
    private final Vector2 tmp = new Vector2();
    private final ShipClickListener shipClickListener;
    private final TaskHandler taskHandler;
    private final MoveCameraTask moveCameraTask;
    private static final Color[] playerColors = {new Color(0.5f, 0, 0, 1), new Color(0, 0.5f, 0, 1), new Color(0, 0, 0.5f, 1), new Color(0, 0.5f, 0.5f, 1)};
    private static final Color[] hiliteColors = {new Color(1f, 0, 0, 1), new Color(0, 1, 0, 1), new Color(0,0,1,1), new Color(0, 1, 1, 1)};
    private final Array<Player> players = new Array<Player>(true, 1);

    private class BattleMessageConsumer extends MessageConsumer {
        public BattleMessageConsumer(MessageDelegator delegator) {
            super(delegator);
        }

        private boolean firstPosSet = false;

        @Override
        public void consume(Player player, Message msg) {
            long updateTime = System.currentTimeMillis();
            if (msg.getType() == Message.MessageType.UPDATE_SHIP_PHYSICS) {
                ShipPhysicsMessage shipPhysicsMessage = (ShipPhysicsMessage) msg;
                ClientShip ship = getShip(shipPhysicsMessage.getId());
                ship.setPosition(shipPhysicsMessage.getX(), shipPhysicsMessage.getY());
                ship.setRotation(shipPhysicsMessage.getAngle());
                ship.setVelocity(shipPhysicsMessage.getVelX(), shipPhysicsMessage.getVelY());
                ship.setAngularVelocity(shipPhysicsMessage.getAngularVelocity(), updateTime);
                ship.setUpdateTime(updateTime);
                if (!firstPosSet) {
                    ship.getCenterPos(tmp);
                    screen.setCameraPos(tmp.x, tmp.y);
                    firstPosSet = true;
                }
            }
            else if (msg.getType() == Message.MessageType.CREATE_SHIP) {
                CreateShipMessage message = (CreateShipMessage) msg;
                log("Creating " + message.getId());
                Player owningPlayer = getPlayer(message.getOwnerId());
                ClientShip ship = new ClientShip(message.getId(), owningPlayer, message.getWidth(),
                        message.getHeight(), message.getMass(), message.getInertia(),
                        message.getMaxLinearForceForward(), message.getMaxLinearForceBackward(),
                        message.getMaxLinearForceLeft(), message.getMaxLinearForceRight(),
                        message.getMaxHealth(), message.getMaxVelocity(), message.getMaxAngularVelocity());
                ships.add(ship);
                screen.getStage().addActor(ship);
                ship.attach(screen.getStage());
                ship.addListener(shipClickListener);


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
                    Player p = new Player(message.getPlayerName(), message.getId(), message.getColorIndex()); // TODO get the color index
                    log("joined in battle:" + p);
                    players.add(p);
                }
            }
        }

        @Override
        public Collection<Message.MessageType> getMessageTypes() {
            return Arrays.asList(Message.MessageType.UPDATE_SHIP_PHYSICS, Message.MessageType.CREATE_SHIP, Message.MessageType.JOIN_SERVER);
        }
    }

    private class ShipClickListener extends ClickListener {
        @Override
        public void clicked (InputEvent event, float x, float y) {
            log("clicked Ship" + event.getTarget() + " @ (" + x + ", " + y + ")");

            if (selectedShip != null) {
                selectedShip.setColor(getBasicColor(selectedShip.getOwner()));
            }
            selectedShip = (ClientShip) event.getTarget();
            selectedShip.clearTargetPos();
            selectedShip.setColor(getHiliteColor(selectedShip.getOwner()));
            event.cancel();
        }
    }

    private Color getHiliteColor(Player player) {
        return hiliteColors[player.getColorIndex()];
    }

    private class StageDragListener extends DragListener {
        @Override
        public void drag(InputEvent event, float x, float y, int pointer) {
            log("dragged!");
        }
    }

    private class StageDragListener2 extends InputListener {
        private boolean dragging;
        private final Vector2 startPoint = new Vector2();

        public StageDragListener2() {
            startPoint.set(screen.getStage().getCamera().position.x, screen.getStage().getCamera().position.y);
        }

        public void touchDragged (InputEvent event, float x, float y, int pointer) {
            if (!dragging) {
                return;
            }

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
            dragging = true;
            startPoint.set(x, y);
            return true;
        }

        public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
            if (dragging) {
                event.cancel();
                dragging = false;
            }
        }
    }

    private class StageClickListener extends ClickListener {
        @Override
        public void clicked (InputEvent event, float x, float y) {
            log("clicked Stage: " + event.getTarget() + " @ (" + x + ", " + y + ")");

            if (selectedShip != null) {
                selectedShip.setTargetPos(x, y);
                screen.setCameraPos(x, y);
                event.stop();
            }
        }

        public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
            if (event.getTarget() instanceof ClientShip) {
                return false;
            }
            return super.touchDown(event, x, y, pointer, button);
        }

        @Override
        public boolean scrolled(InputEvent event, float x, float y, int amount) {
            screen.zoom(amount);
            return true;
        }

        @Override
        public boolean keyDown (InputEvent event, int keycode) {
            if (keycode == Input.Keys.Z) {
                screen.zoom(1);
                return true;
            }
            if (keycode == Input.Keys.X) {
                screen.zoom(-1);
                return true;
            }

            return false;
        }
    }

    private final ServerConnection conn;
    private final BattleScreen screen;
    private final ShipSteering shipSteering;

    private final ArrayList<ClientShip> ships = new ArrayList<ClientShip>();

    private ClientShip selectedShip = null;

    public BattleHandler(BattleScreen screen, ServerConnection conn) {
        this.screen = screen;
        this.conn = conn;
        this.stageClickListener = new StageClickListener();
        this.shipClickListener = new ShipClickListener();
        screen.getStage().addListener(stageClickListener);
        screen.getStage().addListener(new StageDragListener2());
        shipSteering = new ShipSteering(ships, conn);
        this.consumer = new BattleMessageConsumer(conn.getDelegator());
        this.taskHandler = new TaskHandler();
        this.moveCameraTask = new MoveCameraTask(screen);
        taskHandler.addToTaskList(moveCameraTask);
    }

    private ClientShip getShip(long id) {
        for (ClientShip ship : ships) {
            if (ship.getId() == id) {
                return ship;
            }
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
        taskHandler.update(delta);
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
}
