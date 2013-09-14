package com.rasanenj.warp;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.rasanenj.warp.entities.ClientShip;
import com.rasanenj.warp.messaging.*;
import com.rasanenj.warp.screens.BattleScreen;
import com.rasanenj.warp.systems.ShipDriver;

import java.util.ArrayList;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class BattleHandler extends InputListener implements MessageConsumer {
    private final ServerConnection conn;
    private final BattleScreen screen;
    private final ShipDriver shipDriver;

    private final ArrayList<ClientShip> ships = new ArrayList<ClientShip>();

    private ClientShip selectedShip = null;

    public BattleHandler(BattleScreen screen, ServerConnection conn) {
        this.screen = screen;
        this.conn = conn;
        screen.getStage().addListener(this);
        shipDriver = new ShipDriver(ships, conn);
    }

    @Override
    public void consume(Player player, Message msg) {
        if (msg.getType() == Message.MessageType.UPDATE_SHIP_PHYSICS) {
            ShipPhysicsMessage shipPhysicsMessage = (ShipPhysicsMessage) msg;
            ClientShip ship = getShip(shipPhysicsMessage.getId());
            ship.setPosition(shipPhysicsMessage.getX(), shipPhysicsMessage.getY());
        }
        else if (msg.getType() == Message.MessageType.CREATE_SHIP) {
            CreateShipMessage message = (CreateShipMessage) msg;
            ClientShip ship = new ClientShip(message.getId());
            ships.add(ship);
            screen.getStage().addActor(ship);
            screen.getStage().addActor(ship.getTargetImg());
            ship.addListener(this);
        }
    }


    private ClientShip getShip(long id) {
        for (ClientShip ship : ships) {
            if (ship.getId() == id) {
                return ship;
            }
        }
        return null;
    }

    @Override
    public void register(MessageDelegator delegator) {
        delegator.register(this, Message.MessageType.UPDATE_SHIP_PHYSICS);
        delegator.register(this, Message.MessageType.CREATE_SHIP);
    }

    public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
        return true;
    }

    @Override
    public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
        Actor target = event.getTarget();

        log("touchUp: " + event.getTarget() + " @ (" + x + ", " + y + ")");

        if (target instanceof ClientShip) {
            selectedShip = (ClientShip) target;
        }
        else {
            if (selectedShip != null) {
                selectedShip.setTargetPos(x, y);
            }
        }
    }

    public void update(float delta) {
        shipDriver.update();
    }
}
