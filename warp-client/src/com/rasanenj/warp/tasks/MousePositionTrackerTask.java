package com.rasanenj.warp.tasks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

/**
 * @author gilead
 */
public class MousePositionTrackerTask implements Task {
    private final Camera cam;
    private boolean active;
    private final Vector3 tmp = new Vector3();


    public MousePositionTrackerTask(Camera cam) {
        this.cam = cam;
    }

    @Override
    public boolean update(float delta) {
        if (!active) {
            return true;
        }

        tmp.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        cam.unproject(tmp);

        return true;
    }

    @Override
    public void removeSafely() {
    }

    public void activate() {
        this.active = true;
    }

    public void disable() {
        this.active = false;
    }

    public float getX() {
        return tmp.x;
    }

    public float getY() {
        return tmp.y;
    }

    public boolean isActive() {
        return active;
    }
}
