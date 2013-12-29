package com.rasanenj.warp.tasks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.rasanenj.warp.BattleHandler;
import com.rasanenj.warp.entities.ClientShip;
import com.rasanenj.warp.screens.BattleScreen;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class ManualSteeringTask implements Task {
    private final BattleHandler.ShipSelection selection;
    private final OrthographicCamera cam;
    private final BattleScreen screen;
    private boolean active = false;
    private long myId = -1;

    private final Vector2 start = new Vector2(), end = new Vector2(), tmp2 = new Vector2();
    private final Vector3 tmp3 = new Vector3();

    public ManualSteeringTask(BattleHandler.ShipSelection selection, BattleScreen screen) {
        this.selection = selection;
        this.screen = screen;
        this.cam = screen.getCam();
        screen.setManualSteeringLine(start, end);
    }

    public boolean isActive() {
        return active;
    }

    public void activate() {
        active = true;
        screen.setManualSteering(true);
        log("manual steering in continous mode");
    }

    public void disable() {
        active = false;
        screen.setManualSteering(false);
        log("manual steering disabled");
    }

    @Override
    public boolean update(float delta) {
        if (active) {
            setStartAndEnd();
            tmp2.set(end);
            tmp2.sub(start);
            float angle = tmp2.angle();

            for (ClientShip ship : selection) {
                if (ship.getOwner().getId() == myId) {
                    ship.clearAllSteering();
                    ship.setTargetDirection(angle);
                }
            }
        }
        return true;
    }

    private void setStartAndEnd() {
        boolean found = false;
        for (ClientShip s : selection) {
            // use the first owned selected ship as the start
            // TODO: use the average of positions instead
            if (s.getOwner().getId() == myId) {
                s.getCenterPos(start);
                tmp3.set(start.x, start.y, 0);
                cam.project(tmp3);
                start.set(tmp3.x, tmp3.y);
                found = true;
                break;
            }
        }
        if (!found) {
            start.set(0, 0);
            end.set(0, 0);
        }
        else {
            end.set(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
        }

    }

    @Override
    public void removeSafely() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setMyId(long myId) {
        this.myId = myId;
    }
}
