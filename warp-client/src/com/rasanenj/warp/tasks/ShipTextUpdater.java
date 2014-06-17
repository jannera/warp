package com.rasanenj.warp.tasks;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.google.gwt.i18n.client.NumberFormat;
import com.rasanenj.warp.DamageModeler;
import com.rasanenj.warp.ShipSelection;
import com.rasanenj.warp.actors.ClientShip;

/**
 * @author gilead
 */
public class ShipTextUpdater implements Task {
    private final Array<ClientShip> ships;
    private final ShipSelection selection;
    private boolean speed;
    private boolean expectedDamage;
    private NumberFormat decimalFormatter = NumberFormat.getFormat("#.#");

    public ShipTextUpdater(Array<ClientShip> ships, ShipSelection selection, boolean speed, boolean expectedDamage){
        this.ships = ships;
        this.speed = speed;
        this.expectedDamage = expectedDamage;
        this.selection = selection;
    }

    private float sinceLastRun = 0f;
    private static final float UPDATE_LIMIT = 1f; // in seconds

    @Override
    public boolean update(float delta) {
        sinceLastRun += delta;
        if (sinceLastRun < UPDATE_LIMIT) {
            return true;
        }

        sinceLastRun -= UPDATE_LIMIT;

        String text;
        for (ClientShip s : ships) {
            text = "";
            if (speed) {
                text += decimalFormatter.format(s.getVelocity().len()) + "\n";
            }
            if (expectedDamage) {
                text += decimalFormatter.format(getExpectedDamage(s));
            }
            s.setText(text);
        }
        return true;
    }

    private float getExpectedDamage(ClientShip target) {
        long targetOwnerId = target.getOwner().getId();
        float expectedDamage = 0;
        for (ClientShip shooter : selection) {
            long ownerId = shooter.getOwner().getId();
            if (ownerId != targetOwnerId) {
                shooter.getCenterPos(tmp);
                target.getCenterPos(tmp2);

                expectedDamage += DamageModeler.getExpectedDamage(tmp, tmp2,
                        shooter.getVelocity(), target.getVelocity(),
                        shooter.getStats(), target.getStats());
            }
        }
        return expectedDamage;
    }

    private Vector2 tmp = new Vector2(), tmp2 = new Vector2();

    @Override
    public void removeSafely() {
    }

    public void setSpeed(boolean b) {
        speed = b;
    }

    public void setExpectedDamage(boolean b) {
        expectedDamage = b;
    }
}
