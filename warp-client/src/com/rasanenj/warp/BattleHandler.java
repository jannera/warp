package com.rasanenj.warp;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.rasanenj.warp.entities.ClientShip;
import com.rasanenj.warp.messaging.*;
import com.rasanenj.warp.screens.BattleScreen;
import com.rasanenj.warp.systems.ShipDriver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class BattleHandler {
    private final BattleMessageConsumer consumer;
    private final BattleInputListener listener;
    private final Vector2 tmp = new Vector2();

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
                ship.setPosition(shipPhysicsMessage.getX(), shipPhysicsMessage.getY());
                ship.setRotation(shipPhysicsMessage.getAngle());
                ship.setVelocity(shipPhysicsMessage.getVelX(), shipPhysicsMessage.getVelY());
                ship.setAngularVelocity(shipPhysicsMessage.getAngularVelocity());
                if (!firstPosSet) {
                    ship.getCenterPos(tmp);
                    screen.setCameraPos(tmp.x, tmp.y);
                    firstPosSet = true;
                }
            }
            else if (msg.getType() == Message.MessageType.CREATE_SHIP) {
                CreateShipMessage message = (CreateShipMessage) msg;
                ClientShip ship = new ClientShip(message.getId(), message.getWidth(),
                        message.getHeight(), message.getMass());
                ships.add(ship);
                screen.getStage().addActor(ship);
                ship.attach(screen.getStage());
                ship.addListener(listener);
            }
        }

        @Override
        public Collection<Message.MessageType> getMessageTypes() {
            return Arrays.asList(Message.MessageType.UPDATE_SHIP_PHYSICS, Message.MessageType.CREATE_SHIP);
        }
    }

    private class BattleInputListener extends InputListener {
        public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
            return true;
        }

        @Override
        public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
            Actor target = event.getTarget();

            log("touchUp: " + event.getTarget() + " @ (" + x + ", " + y + ")");

            if (target instanceof ClientShip) {
                selectedShip = (ClientShip) target;
                selectedShip.clearTargetPos();
            }
            else {
                if (selectedShip != null) {
                    selectedShip.setTargetPos(x, y);
                    screen.setCameraPos(x, y);
                }
            }
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
    private final ShipDriver shipDriver;

    private final ArrayList<ClientShip> ships = new ArrayList<ClientShip>();

    private ClientShip selectedShip = null;

    public BattleHandler(BattleScreen screen, ServerConnection conn) {
        this.screen = screen;
        this.conn = conn;
        this.listener = new BattleInputListener();
        screen.getStage().addListener(listener);
        shipDriver = new ShipDriver(ships, conn);
        this.consumer = new BattleMessageConsumer(conn.getDelegator());
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
        for (ClientShip s : ships) {
            // TODO: make this update depend on the time has elapsed from last position update
            // TODO: maybe also include acceleration data
            // s.updatePos(delta);
        }
        consumer.consumeStoredMessages();
        shipDriver.update();
    }
}
