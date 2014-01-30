package com.rasanenj.warp;

import com.rasanenj.warp.entities.ClientShip;

/**
 * @author gilead
 */
public class ActiveShipSelection extends ShipSelection {
    public void clear() {
        for (ClientShip s : selectedShips) {
            s.getImage().setColor(Assets.getBasicColor(s.getOwner()));
        }
        super.clear();
    }

    public void add(ClientShip ship) {
        ship.getImage().setColor(Assets.getHiliteColor(ship.getOwner()));
        super.add(ship);
    }
}
