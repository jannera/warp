package com.rasanenj.warp.ai;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.rasanenj.warp.DamageModeler;
import com.rasanenj.warp.PositionProjection;
import com.rasanenj.warp.entities.ClientShip;
import com.rasanenj.warp.systems.ShipShooting;

import static com.rasanenj.warp.Log.log;

/**
 * // TODO:
 * // this could be made non-static, so that each own ClientShip could have
 * // it's own AI.. because most of the time, the decision tree remains unchanged
 *
 * @author gilead
 *
 */
public class ShipShootingAI {
    private static final Vector2 shooterPos = new Vector2(),
            targetPos = new Vector2();

    public static class Decision {
        public Array<Decision> children = new Array<Decision>(false, 2);
        public float value;

        public Decision getMostValuableChild() {
            float maxValue = Float.MIN_VALUE;
            Decision mostValuableChild = null;
            for (Decision d : children) {
                if (d.value > maxValue) {
                    maxValue = d.value;
                    mostValuableChild = d;
                }
            }
            return mostValuableChild;
        }

        public void logTree() {
            log(toString(0));
        }

        private String toString(int level) {
            String output = "";
            String padding = "--";
            output += new String(new char[level]).replace("\0", padding);
            output += "> ";
            String className = "Root";
            if (this instanceof WaitDecision) {
                className = "Wait";
            }
            else if (this instanceof ShootDecision) {
                className = "Shoot";
            }
            output += className;
            output += " :";
            output += value;
            output += '\n';
            for (Decision d : children) {
                output += d.toString(level + 1);
            }
            return output;
        }

        public ClientShip getTarget() {
            return null;
        }
    }

    public static class WaitDecision extends Decision {
        public WaitDecision parent;

        public void calculateValueFromChildren() {
            float maxValue = Float.MIN_VALUE;
            for (Decision d : children) {
                if (d.value > maxValue) {
                    maxValue = d.value;
                }
            }
            value = maxValue;
        }
    }

    public static class ShootDecision extends Decision {
        private final ClientShip target;

        private ShootDecision(ClientShip target, float value) {
            this.target = target;
            this.value = value;
            log("created shoot decision with value " + value);
        }

        public static ShootDecision createDecision(ClientShip shooter, ClientShip target,
                                                   Vector2 shooterPos, Vector2 targetPos,
                                                   Vector2 shooterVel, Vector2 targetVel,
                                                   long extraWaitTime) {
            float expectedDamage = DamageModeler.getExpectedDamage(shooterPos, targetPos,
                    shooterVel, targetVel, shooter.getStats(), target.getStats());
            log("expected damage was " + expectedDamage);
            float time = shooter.getStats().getWeaponCooldown() + extraWaitTime / 1000f;
            float value = expectedDamage / time;
            // TODO: reduce the value by some kind of optimism/pessimism multiplier
            return new ShootDecision(target, value);
        }

        public ClientShip getTarget() {
            return target;
        }
    }

    // null means do not fire yet
    public static ClientShip getFiringTarget(ClientShip shooter) {
        if (shooter.getFiringTarget() == null) {
            return null;
        }
        Decision treeRoot = buildTree(shooter);

        treeRoot.logTree();

        Decision best = treeRoot.getMostValuableChild();
        return best.getTarget();
    }

    private static Decision buildTree(ClientShip shooter) {
        Decision root = new Decision();

        ClientShip target = shooter.getFiringTarget();

        // first, the "shoot now" option
        shooter.getCenterPos(shooterPos);
        target.getCenterPos(targetPos);
        ShootDecision shoot = ShootDecision.createDecision(shooter, target,
                shooterPos, targetPos, shooter.getVelocity(), target.getVelocity(), 0);
        root.children.add(shoot);

        WaitDecision wait = new WaitDecision();
        root.children.add(wait);

        // all the waiting + projections
        Array<PositionProjection> shooterProjections = shooter.getProjectedPositions();
        Array<PositionProjection> targetProjections = target.getProjectedPositions();

        WaitDecision current = wait;
        for (int i =0; i < ShipShooting.PROJECTION_POINTS_AMOUNT; i++) {
            PositionProjection shooterProjection = shooterProjections.get(i);
            PositionProjection targetProjection = targetProjections.get(i);
            log(shooterProjection.getPosition() + " vs " + targetProjection.getPosition());
            shoot = ShootDecision.createDecision(shooter, target,
                    shooterProjection.getPosition(),
                    targetProjection.getPosition(),
                    shooter.getVelocity(), // TODO velocities should be taken from the projection
                    target.getVelocity(),
                    (i + 1) * ShipShooting.PROJECTION_INTERVAL_MS);
            current.children.add(shoot);

            if (i < ShipShooting.PROJECTION_POINTS_AMOUNT - 1) {
                wait = new WaitDecision();
                current.children.add(wait);
                wait.parent = current;
                current = wait;
            }
        }

        // calculate the total values for wait nodes, bottom up
        do {
            current.calculateValueFromChildren();
            current = current.parent;
        } while(current != null);

        return root;
    }
}
