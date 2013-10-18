package com.rasanenj.warp.tasks;

import com.badlogic.gdx.physics.box2d.Body;
import com.rasanenj.warp.entities.ServerShip;

import java.util.ArrayList;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class VelocityPrinter implements Task {
    private final ArrayList<ServerShip> ships;
    private float oldVel = 0;

    public VelocityPrinter(ArrayList<ServerShip> ships) {
        this.ships = ships;
    }

    private final long INTERVAL = 1000; // in ms
    private long lastUpdate = 0;
    int counter = 0;

    @Override
    public boolean update(float delta) {
        long timeNow = System.currentTimeMillis();
        if (timeNow - lastUpdate > INTERVAL) {
            for (ServerShip ship : ships) {
                Body b = ship.getBody();
                float vel = b.getLinearVelocity().len();
                // float vel = b.getAngularVelocity();
                if (vel != 0) {
                    // log(counter + " " + ship.getId() + " : " + (vel - oldVel));
                    // log (vel);
                    counter++;
                }
                oldVel = vel;
            }
            lastUpdate = timeNow;
        }
        return true;
    }

    @Override
    public void removeSafely() {
    }
}
