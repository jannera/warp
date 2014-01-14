package com.rasanenj.warp;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.rasanenj.warp.entities.ClientShip;
import com.rasanenj.warp.tasks.MousePositionTrackerTask;
import com.rasanenj.warp.tasks.MouseShipSelector;
import com.rasanenj.warp.tasks.TaskHandler;

import java.util.ArrayList;

/**
 * @author gilead
 */
public class OrbitUIHandler {
    private final MousePositionTrackerTask orbitPosTask;
    private final MouseShipSelector mouseShipSelector;

    private final Vector2 tmp = new Vector2(), tmp2 = new Vector2();
    private final BattleHandler.ShipSelection selection;

    private ClientShip orbitTargetShip;

    private State state;

    public enum State {
        DISABLED, SELECTING_TARGET, SELECTING_RADIUS
    }

    public OrbitUIHandler(TaskHandler taskHandler, Camera cam, ArrayList<ClientShip> ships,
                          BattleHandler.ShipSelection selection) {
        this.mouseShipSelector = new MouseShipSelector(cam, ships);
        taskHandler.addToTaskList(mouseShipSelector);
        orbitPosTask = new MousePositionTrackerTask(cam);
        orbitPosTask.disable();
        taskHandler.addToTaskList(orbitPosTask);
        state = State.DISABLED;
        this.selection = selection;
    }

    public State getState() {
        return state;
    }

    public void setState(State newState) {
        if (newState == State.SELECTING_TARGET) {
            mouseShipSelector.activate(everythingButSelectionFilter);
        }
        else if (newState == State.SELECTING_RADIUS) {
            orbitTargetShip = mouseShipSelector.getClosest();
            if (orbitTargetShip == null) {
                setState(State.DISABLED);
            }
            else {
                mouseShipSelector.disable();
                orbitPosTask.activate();
            }
        }
        else if (newState == State.DISABLED) {
            mouseShipSelector.clearCircles();
            mouseShipSelector.disable();
            orbitPosTask.disable();
            orbitTargetShip = null;
        }
        state = newState;
    }

    public void setOrbit(boolean clockWise) {
        if (orbitTargetShip != null) {
            orbitTargetShip.getCenterPos(tmp2);
            for (ClientShip s : selection) {
                if (s == orbitTargetShip) {
                    // don't tell a ship to orbit itself
                    continue;
                }
                s.getCenterPos(tmp);
                float dst2 = getOrbitRadius2();
                s.setOrbit(orbitTargetShip, dst2, clockWise);
            }
        }
    }

    public float getOrbitRadius() {
        return (float) Math.pow(getOrbitRadius2(), 0.5);
    }

    private float getOrbitRadius2() {
        tmp.set(orbitPosTask.getX(), orbitPosTask.getY());
        orbitTargetShip.getCenterPos(tmp2);
        return tmp.dst2(tmp2);
    }

    private final MouseShipSelector.ClientShipFilter everythingButSelectionFilter = new MouseShipSelector.ClientShipFilter() {
        @Override
        public boolean contains(ClientShip ship) {
            return !selection.contains(ship);
        }
    };

    public ClientShip getOrbitTargetShip() {
        return orbitTargetShip;
    }
}
