package com.rasanenj.warp.tasks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.rasanenj.warp.Assets;
import com.rasanenj.warp.entities.ClientShip;

/**
 * @author gilead
 */
public class MouseShipSelector implements Task {
    private final Camera cam;
    private final Vector3 tmp = new Vector3();
    private final Vector2 tmp2 = new Vector2();
    private final Iterable<ClientShip> ships;
    private boolean active;
    private ClientShipFilter filter;
    private ClientShip closest;

    public interface ClientShipFilter {
        public abstract boolean contains(ClientShip ship);
    }

    public MouseShipSelector(Camera cam, Iterable<ClientShip> ships) {
        this.cam = cam;
        this.ships = ships;
        this.active = false;
    }

    @Override
    public boolean update(float delta) {
        closest = null;
        if (!active) {
            return true;
        }

        tmp.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        cam.unproject(tmp);

        float lowestDst = Float.MAX_VALUE;
        closest = null;

        for (ClientShip ship : ships) {
            if (!filter.contains(ship)) {
                continue;
            }
            ship.getCenterPos(tmp2);
            float dst = tmp2.dst2(tmp.x, tmp.y);
            if (dst < lowestDst) {
                closest = ship;
                lowestDst = dst;
            }
        }

        for (ClientShip ship : ships) {
            if (ship != closest) {
                ship.clearCircle();
            }
        }

        if (closest != null) {
            closest.setCircled(Assets.newCommandsColor);
        }

        return true;
    }

    public void activate(ClientShipFilter filter) {
        this.active = true;
        this.filter = filter;
    }

    public void disable() {
        this.active = false;
    }

    public void clearCircles() {
        for (ClientShip ship : ships) {
            ship.clearCircle();
        }
    }

    @Override
    public void removeSafely() {
    }

    public ClientShip getClosest() {
        return closest;
    }
}
