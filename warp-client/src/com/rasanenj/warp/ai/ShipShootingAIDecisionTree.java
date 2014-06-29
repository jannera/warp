package com.rasanenj.warp.ai;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.rasanenj.warp.DamageModeler;
import com.rasanenj.warp.Log;
import com.rasanenj.warp.PositionProjection;
import com.rasanenj.warp.actors.ClientShip;
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
public class ShipShootingAIDecisionTree implements ShipShootingAI {
    private final Array<ClientShip> ships;

    public ShipShootingAIDecisionTree(Array<ClientShip> ships) {
        this.ships = ships;
    }

    private static final float MIN_TRY_CHANCE = 0.01f;

    public static class Decision {
        public Array<ShootDecision> shootDecisions = new Array<ShootDecision>(false, 2);
        public WaitDecision waitDecision;
        public float value;
        public float chance;
        public final int projectionIndex;

        public Decision(int projectionIndex) {
            this.projectionIndex = projectionIndex;
        }

        public Decision getMostValuableChild() {
            float maxValue = waitDecision.value;
            Decision mostValuableChild = waitDecision;
            for (Decision d : shootDecisions) {
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

        protected String toString(int level) {
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
            for (Decision d : shootDecisions) {
                output += d.toString(level + 1);
            }
            if (waitDecision != null) {
                output += waitDecision.toString(level + 1);
            }
            return output;
        }

        public ClientShip getTarget() {
            return null;
        }

        // updates the tree that has been built before
        public void update(ClientShip shooter) {
            WaitDecision last = null;
            Decision current = this;
            while (current != null) {
                for (ShootDecision shootDecision : current.shootDecisions) {
                    shootDecision.updateDecision(shooter);
                }
                if (current.waitDecision != null) {
                    last = current.waitDecision;
                }
                current = current.waitDecision;
            }

            // calculate the total values for wait nodes, bottom up
            while(last != null) {
                last.calculateValueFromChildren();
                last = last.parent;
            }
        }
    }

    private static class WaitDecision extends Decision {
        public WaitDecision parent;

        public WaitDecision(int projectionIndex) {
            super(projectionIndex);
        }

        public void calculateValueFromChildren() {
            float maxValue = 0;
            for (Decision d : shootDecisions) {
                if (d.value > maxValue) {
                    maxValue = d.value;
                }
            }
            if (waitDecision != null && waitDecision.value > maxValue) {
                maxValue = waitDecision.value;
            }
            value = maxValue;
        }
    }

    private static class ShootDecision extends Decision {
        private ClientShip target;

        public ShootDecision(int projectionIndex, ClientShip target) {
            super(projectionIndex);
            this.target = target;
        }

        private static final Vector2 shooterPos = new Vector2();
        private static final Vector2 targetPos = new Vector2();
        private static final Vector2 shooterVel = new Vector2();
        private static final Vector2 targetVel = new Vector2();

        public void updateDecision(ClientShip shooter) {
            if (projectionIndex == -1) {
                shooter.getCenterPos(shooterPos);
                target.getCenterPos(targetPos);
                shooterVel.set(shooter.getVelocity());
                targetVel.set(target.getVelocity());
            }
            else {
                PositionProjection shooterProjection = shooter.getProjectedPositions().get(projectionIndex);
                shooterPos.set(shooterProjection.getPosition());
                shooterVel.set(shooterProjection.getVelocity());
                PositionProjection targetProjection = target.getProjectedPositions().get(projectionIndex);
                targetPos.set(targetProjection.getPosition());
                targetVel.set(targetProjection.getVelocity());
            }
            this.chance = DamageModeler.getChance(shooterPos, targetPos,
                    shooterVel, targetVel, shooter.getStats(), target.getStats());
            float expectedDamage = DamageModeler.getExpectedDamage(chance, shooter.getStats());
            // log("expected damage was " + expectedDamage);
            float extraWaitTime = ((float) (projectionIndex + 1)) * (float) ShipShooting.PROJECTION_INTERVAL_MS;
            float time = shooter.getStats().getWeaponCooldown() + extraWaitTime / 1000f;
            this.value = expectedDamage / time * (float) (target.getTargetValue() + 1);
            // TODO: reduce the value by some kind of optimism/pessimism multiplier
        }

        public ClientShip getTarget() {
            return target;
        }
    }

    // null means do not fire yet
    public ClientShip getFiringTarget(ClientShip shooter) {
        Decision root = shooter.getDecisionTreeRoot();
        if (shooter.isDecisionTreeDirty()) {
            Log.log("building new target tree for ship " + shooter.getId());
            root = buildTree(shooter);
            shooter.setDecisionTreeRoot(root);
            shooter.setDecisionTreeDirty(false);
        }

        root.update(shooter);
        // root.logTree();

        Decision best = root.getMostValuableChild();
        ClientShip target = best.getTarget();
        if (target != null) {
            if (best.chance < MIN_TRY_CHANCE) {
                // log("decided to wait with chance " + best.chance);
                return null;
            }
            log("decided to shoot with chance " + best.chance);
        }
        else {
            // log("decided to wait because seeing a better chance in future");
        }
        return target;
    }

    private Decision buildTree(ClientShip shooter) {
        Decision root = new Decision(-1);

        addShootDecisions(-1, shooter, root.shootDecisions);

        WaitDecision wait = new WaitDecision(-1);
        root.waitDecision = wait;

        if (root.shootDecisions.size == 0) {
            // if there's nothing to shoot, don't build rest of the tree.. wait decision is the only decision
            return root;
        }

        WaitDecision current = wait;
        for (int i =0; i < ShipShooting.PROJECTION_POINTS_AMOUNT; i++) {
            addShootDecisions(i, shooter, current.shootDecisions);

            if (i < ShipShooting.PROJECTION_POINTS_AMOUNT - 1) {
                wait = new WaitDecision(i);
                current.waitDecision = wait;
                wait.parent = current;
                current = wait;
            }
        }

        return root;
    }

    private void addShootDecisions(int projectionIndex, ClientShip shooter, Array<ShootDecision> shootDecisions) {
        Long ownerId = shooter.getOwner().getId();
        for (int i=0; i < ships.size; i++) {
            ClientShip s = ships.get(i);
            if (s.getOwner().getId() != ownerId) {
                shootDecisions.add(new ShootDecision(projectionIndex, s));
            }
        }
    }
}
