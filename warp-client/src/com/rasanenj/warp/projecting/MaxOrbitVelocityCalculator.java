package com.rasanenj.warp.projecting;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Array;
import com.rasanenj.warp.actors.ClientShip;
import com.rasanenj.warp.entities.OrbitVelocities;
import com.rasanenj.warp.entities.ShipStats;
import com.rasanenj.warp.screens.BattleScreen;

import static com.rasanenj.warp.Log.log;

/**
 * For given ship, builds the maximum velocity chart for various orbit distances.
 * Accomplishes this by simulating the orbiting at various velocities
 * until an acceptable orbit path is achieved.
 *
 * @author gilead
 */
public class MaxOrbitVelocityCalculator {
    private final ShipStats dummyStats = new ShipStats(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1, ShipStats.Shiptype.FRIGATE);
    private final ClientShip orbitShip = new ClientShip(-1, null, dummyStats);
    private final Vector2 tmp = new Vector2(), tmp2 = new Vector2();

    private final PositionProjector projector;
    private final int ORBIT_PROJETION_POINTS_AMOUNT;
    private final BattleScreen screen;
    private final long projectionTimeMS;

    public MaxOrbitVelocityCalculator(long projectionTimeMS, float updatesInSecond, BattleScreen screen) {
        this.projectionTimeMS = projectionTimeMS;
        final int PROJECTION_INTERVAL_MS = (int) (1000f / updatesInSecond);
        ORBIT_PROJETION_POINTS_AMOUNT = (int) ((float) projectionTimeMS / (float) PROJECTION_INTERVAL_MS);
        projector = new PositionProjector(updatesInSecond, projectionTimeMS);

        orbitShip.setX(0);
        orbitShip.setY(0);
        orbitShip.setRotation(0);
        orbitShip.setVisible(false);
        this.screen = screen;
    }

    private static final float[] results = new float[10];

    public void calcOrbitingVelocities(ClientShip ship) {
        orbitShip.setSize(ship.getWidth(), ship.getHeight());

        final float maxOrbitRange = 12f; // todo: how to decide orbit ranges?

        ship.initProjections(ORBIT_PROJETION_POINTS_AMOUNT);
        float maxVel = ship.getStats().getMaxLinearVelocity();

        Array<OrbitVelocities.VelocityLimit> limits = ship.getStats().getOrbitVelocities().getLimits();

        final int limitAmount = 10; // todo: how to decide the amount of limits?

        for (int i=1; i <= limitAmount; i++) {
            float orbitRange = maxOrbitRange * i/10f;
            orbitRange = orbitRange * orbitRange;
            limits.add(new OrbitVelocities.VelocityLimit(orbitRange, maxVel));
        }
        // log(ship.getId() + ":" + ship.getStats().getOrbitVelocities());

        float errorMargin = 0.10f;

        orbitShip.getCenterPos(tmp);

        /**
         * For every orbit distance, tries 100.. 90.. 80.. % velocity
         * until an acceptable orbit is reached
         */

        for (int i=0; i < limits.size; i++) {
            OrbitVelocities.VelocityLimit limit = limits.get(i);
            float orbitRange2 = limit.dst2;
            float orbitRange = (float) Math.sqrt(orbitRange2);

            ship.setOrbit(orbitShip, orbitRange2, false);

            // try using first 100% vel, then 90%, 80%, ...
            // until a solution is found
            final float orbitLimitLow = orbitRange * (1f - errorMargin);
            final float orbitLimitHigh = orbitRange * (1f + errorMargin);
            for (int j=0; j < 10; j++) {
                float velocityLimit = maxVel * (1f - j/10f);
                for (int k=0; k < limits.size; k++){
                    limits.get(k).velocity = velocityLimit;
                }
                ship.setX(orbitShip.getX());
                ship.setY(orbitShip.getY() - orbitRange);
                ship.setRotation(0);

                ship.getCenterPos(tmp2);
                projector.project(ship);

                // log("i=" + i + " j=" + j + ": " + ship.getProjectedPositions().first().getPosition());

                boolean allOkay = true;
                // check if the projected curve is stable enough
                for(PositionProjection projection : ship.getProjectedPositions()) {
                    float dst = projection.getPosition().dst(tmp);
                    if (dst > orbitLimitHigh) {
                        allOkay = false;
                        break;
                    }
                    if (dst < orbitLimitLow) {
                        allOkay = false;
                        break;
                    }
                }
                /*
                String s = allOkay ? "s" : "e";
                s += " i " + i + " j " + j;
                logPositionProjection(s, ship.getProjectedPositions(), orbitLimitHigh * 2.1f, orbitLimitHigh * 2.1f,
                        tmp, orbitLimitLow, orbitLimitHigh);
                        */
                if (allOkay) {
                    results[i] = velocityLimit;
                    break;
                }
            }
        }

        for (int i=0; i < limitAmount; i++) {
            limits.get(i).velocity = results[i];
        }

        log(ship.getId() + ":" + ship.getStats().getOrbitVelocities());
    }

    private void logPositionProjection(String s, Array<PositionProjection> projectedPositions, float width, float height,
                                       Vector2 offset, float orbitLimitLow, float orbitLimitHigh) {
        Window w = screen.getDebugWindow().getWindow();
        //float windowHeight = w.getHeight(), windowWidth = w.getWidth();
        float windowHeight = 256, windowWidth = 256;

        Pixmap pixmap = new Pixmap((int)windowHeight, (int)windowWidth, Pixmap.Format.RGBA8888);

        // render all unallowed background black, and a gray strip in the allowed area
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        pixmap.setColor(Color.GRAY);
        int halfWidth = (int) (windowWidth/2f), halfHeight = (int) (windowHeight/2f);
        pixmap.fillCircle(halfWidth, halfHeight, (int) (orbitLimitHigh/width * windowWidth));
        pixmap.setColor(Color.BLACK);
        pixmap.fillCircle(halfWidth, halfHeight, (int) (orbitLimitLow/width * windowWidth));

        Color c;
        // log(projectedPositions.get(0).getPosition().x - offset.x);

        for (PositionProjection p : projectedPositions) {
            tmp2.set(p.getPosition());
            float dst = tmp2.dst(offset);
            if (dst > orbitLimitHigh || dst < orbitLimitLow) {
                c = Color.RED;
            }
            else {
                c = Color.GREEN;
            }
            tmp2.sub(offset);
            tmp2.x /= width;
            tmp2.y /= height;
            tmp2.x *= windowWidth;
            tmp2.y *= windowHeight;

            // now origo is in the center
            tmp2.x += windowWidth / 2f;
            tmp2.y += windowHeight / 2f;

            float scale = 1f - 0.75f * (float) p.getTimestamp() / (float) projectionTimeMS;
            pixmap.setColor(scale * c.r, scale * c.g, scale * c.b, 1);
            pixmap.drawCircle((int) tmp2.x, (int)tmp2.y, 3);
        }

        Texture t = new Texture(pixmap);
        Image i = new Image(t);
        screen.getDebugWindow().addActor(s, i);
        w.pack();
    }
}
