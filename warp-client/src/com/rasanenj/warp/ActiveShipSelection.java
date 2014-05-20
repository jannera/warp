package com.rasanenj.warp;

import com.rasanenj.warp.actors.ClientShip;

/**
 * @author gilead
 */
public class ActiveShipSelection extends ShipSelection {
    public void clear() {
        for (ClientShip s : selectedShips) {
            s.setSelected(false);
        }
        super.clear();
    }

    public void add(ClientShip ship) {
        ship.setSelected(true);
        super.add(ship);
    }
}
