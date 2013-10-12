package com.rasanenj.warp.tasks;

import com.badlogic.gdx.math.Vector2;
import com.rasanenj.warp.screens.BattleScreen;

import static com.rasanenj.warp.Log.log;

/**
 * @author Janne Rasanen
 */
public class MoveCameraTask implements Task {
    private final Vector2 tgt = new Vector2();
    private final BattleScreen screen;

    public MoveCameraTask(BattleScreen screen) {
        this.screen = screen;
    }


    @Override
    public boolean update(float delta) {
        // log(tgt);
        screen.translateCamera(tgt.x, tgt.y);
        tgt.set(0, 0);
        return true;
    }

    @Override
    public void removeSafely() {

    }

    public void setTarget(float x, float y) {
        // log("adding " + x + ", " + y);
        tgt.set(x, y);
    }
}
