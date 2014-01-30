package com.rasanenj.warp;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.rasanenj.warp.entities.ClientShip;
import com.rasanenj.warp.messaging.Player;

import java.util.Iterator;

/**
 * @author gilead
 */
public class ShipSelection implements Iterable<ClientShip> {
    protected Array<ClientShip> selectedShips = new Array<ClientShip>(false, 16);
    protected final Vector2 lastWeightedPos = new Vector2(Float.NaN, Float.NaN),
            posDiff = new Vector2(0, 0), tmp = new Vector2(0, 0);

    public void clear() {
        selectedShips.clear();
    }

    public void add(ClientShip ship) {
        selectedShips.add(ship);
        calcWeightedPos(lastWeightedPos);
    }

    public void remove(ClientShip ship) {
        selectedShips.removeValue(ship, true);
        calcWeightedPos(lastWeightedPos);
    }

    @Override
    public Iterator<ClientShip> iterator() {
        return selectedShips.iterator();
    }

    private void calcWeightedPos(Vector2 tgt) {
        if (selectedShips.size == 0) {
            tgt.set(Float.NaN, Float.NaN);
            return;
        }

        tgt.set(0, 0);
        for (ClientShip ship : this) {
            tgt.add(ship.getX(), ship.getY());
        }
        tgt.scl(1f / selectedShips.size);
    }

    /**
     * Calculates the change in weighted position compared to last frame
     * and stores the current position.
     */
    public Vector2 getPosDiff() {
        if (selectedShips.size == 0) {
            return posDiff.set(0, 0);
        }

        calcWeightedPos(posDiff);
        tmp.set(posDiff);
        posDiff.sub(lastWeightedPos);
        lastWeightedPos.set(tmp);
        return posDiff;
    }

    public boolean contains(ClientShip ship) {
        return selectedShips.contains(ship, true);
    }

    public void setDesiredRelativeVelocity(float velocity) {
        for (ClientShip s : this) {
            s.setDesiredRelativeVelocity(velocity);
        }
    }

    public void set(Iterable<ClientShip> newSet) {
        clear();
        for (ClientShip s : newSet) {
            add(s);
        }
    }
}
