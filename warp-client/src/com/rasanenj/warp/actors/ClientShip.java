package com.rasanenj.warp.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.rasanenj.warp.Assets;
import com.rasanenj.warp.projecting.PositionProjection;
import com.rasanenj.warp.TargetValue;
import com.rasanenj.warp.ai.ShipShootingAIDecisionTree;
import com.rasanenj.warp.entities.ShipStats;
import com.rasanenj.warp.messaging.Player;
import com.rasanenj.warp.systems.ShipSteering;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class ClientShip extends Group {
    private final float MAX_DST_CHANGE_PER_FRAME, MIN_TELEPORT_DST;
    private final Player owner;
    private long lastFiringTime = 0;
    private float health;

    private static long lastid = 0;

    private ShipStats stats;

    private final Image baseImage, hiliteImage, clickRegionImage;

    private Vector2[] vertices;
    private String text = "";
    private float targetDirection = Float.NaN;
    private boolean orbitCW;
    private ClientShip orbitShip;
    private float orbitDst2;
    private Color circled;
    private float lastServerRotation;
    private Array<PositionProjection> projectedPositions;
    private TargetValue targetValue;
    public static final int MAX_TARGET_VALUE = 4;

    private ShipShootingAIDecisionTree.Decision decisionTreeRoot;
    private boolean decisionTreeDirty = true; // set this true to rebuild whole decision tree before updating it


    public ClientShip(long id, Player owner, ShipStats stats) {
        if (owner != null) {
            this.baseImage = new Image(Assets.getShipBaseTexture(stats.getType(), owner));
            this.clickRegionImage = new Image(Assets.getShipBaseTexture(stats.getType(), owner));
            this.hiliteImage = new Image(Assets.getShipHiliteTexture(stats.getType(), owner));
            this.clickRegionImage.setColor(1, 1, 1, 0);
            this.clickRegionImage.setVisible(true);
            float width = stats.getWidth();
            float height = stats.getHeight();
            clickRegionImage.setWidth(width * getClickRegionMultiplier(stats.getType()));
            clickRegionImage.setHeight(height * getClickRegionMultiplier(stats.getType()));
            addActor(baseImage);
            addActor(hiliteImage);
            addActor(clickRegionImage);
            this.setWidth(width);
            this.setHeight(height);
            baseImage.setWidth(width);
            baseImage.setHeight(height);
            hiliteImage.setWidth(width);
            hiliteImage.setHeight(height);
            // make the hovering and non-hovering ships overlap on the center
            clickRegionImage.setPosition(baseImage.getWidth() / 2f - clickRegionImage.getWidth() / 2f,
                    baseImage.getHeight() / 2f - clickRegionImage.getHeight() / 2f);

            targetValue = TargetValue.others;
        }
        else {
            // TODO: remove this, instead make superclass/interface or something for steering position prediction
            baseImage = null;
            hiliteImage = null;
            clickRegionImage = null;
        }

        this.stats = stats;
        lastid = id;
        this.id = id;
        this.owner = owner;

        setVisible(false);

        this.stats.scaleForces(ShipSteering.STEP_LENGTH);
        this.health = stats.getMaxHealth();

        this.clearAllSteering();
        MAX_DST_CHANGE_PER_FRAME = stats.getMaxLinearVelocity() * 4f / 60f;
        MIN_TELEPORT_DST = MAX_DST_CHANGE_PER_FRAME * 60f;
    }

    public void initProjections(final int amount) {
        projectedPositions = new Array<PositionProjection>(true, amount);
        for(int i = 0; i < amount; i++) {
            projectedPositions.add(new PositionProjection());
        }
    }

    // how many times bigger area should work as clicking area around the ship
    private float getClickRegionMultiplier(ShipStats.Shiptype type) {
        switch (type) {
            case FRIGATE:
                return 4f;
            case CRUISER:
                return 2f;
            case BATTLESHIP:
                return 1f;
        }
        return 1f;
    }

    private Vector2 impulseIdeal = new Vector2();

    public void setImpulseIdeal(Vector2 impulseIdeal) {
        this.impulseIdeal.set(impulseIdeal);
    }

    public Vector2 getImpulseIdeal() {
        return impulseIdeal;
    }

    /**
     * Returns the corners of a rotated rectangle that limits the
     * maximum force a ship can have for moving around.
     *
     * Corners will be in world-coordinates and centered on
     * the center of the ship.
     *
     * @param corners where corners will be stored
     */
    public void getForceLimitCorners(Vector2[] corners) {
        corners[0].x = stats.getMaxLinearForceForward();
        corners[0].y = stats.getMaxLinearForceLeft();

        corners[1].x = stats.getMaxLinearForceForward();
        corners[1].y = -stats.getMaxLinearForceRight();

        corners[2].x = -stats.getMaxLinearForceBackward();
        corners[2].y = -stats.getMaxLinearForceRight();

        corners[3].x = -stats.getMaxLinearForceBackward();
        corners[3].y = stats.getMaxLinearForceLeft();

        getCenterPos(tmp);
        for (int i=0; i < 4; i++) {
            corners[i].rotate(getRotation());
            corners[i].add(tmp);
        }
    }

    public boolean canFire() {
        if (System.currentTimeMillis() - lastFiringTime > stats.getWeaponCooldown() * 1000) {
            return true;
        }
        return false;
    }

    public float getFiringReadiness() {
        return MathUtils.clamp(
                (System.currentTimeMillis() - lastFiringTime) /
                        (stats.getWeaponCooldown() * 1000), 0, 1);
    }

    public void reduceHealth(float damage) {
        health -= damage;
    }

    public void setVertices(Vector2[] vertices) {
        this.vertices = vertices;
    }

    public Vector2[] getVertices() {
        return vertices;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTargetDirection(float targetDirection) {
        this.targetDirection = targetDirection;
    }

    public float getTargetDirection() {
        return targetDirection;
    }

    public void clearTargetDirection() {
        targetDirection = Float.NaN;
    }

    public void clearAllSteering() {
        clearTargetDirection();
        clearOrbit();
        clearTargetPos();
    }

    public void setOrbit(ClientShip target, float dst2, boolean clockwise) {
        clearAllSteering();
        orbitShip = target;
        orbitDst2 = dst2;
        orbitCW = clockwise;
    }

    public void clearOrbit() {
        orbitShip = null;
    }

    public boolean hasOrbitTarget() {
        return orbitShip != null;
    }

    public void setCircled(Color circled) {
        this.circled = circled;
    }

    public boolean isCircled() {
        return circled != null;
    }

    public Color getCircleColor() {
        return circled;
    }

    public void clearCircle() {
        circled = null;
    }

    public boolean hasSteeringTarget() {
        return hasDirectionTarget() || hasOrbitTarget() || hasTargetPos();
    }

    public void copySimulationStats(ClientShip ship) {
        setPosition(ship.getX(), ship.getY());
        setRotation(ship.getRotation());
        stats = ship.stats;
        velocity.set(ship.getVelocity());
        targetPos.set(ship.getTargetPos());
        targetDirection = ship.getTargetDirection();
        orbitCW = ship.isOrbitCW();
        orbitShip = ship.getOrbitShip();
        orbitDst2 = ship.getOrbitDst2();
        angularVelocity = ship.getAngularVelocity();
        setWidth(ship.getWidth());
        setHeight(ship.getHeight());
    }

    public TargetValue getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(TargetValue targetValue) {
        this.targetValue = targetValue;
    }

    public enum TurningState {
        FULL_SPEED, BRAKING, DONE_BRAKING;
    }

    TurningState turningState;

    Vector2 targetPos = new Vector2();
    Vector2 velocity = new Vector2();
    Vector2 impulse = new Vector2();
    Vector2 lastServerPosition = new Vector2();

    private static final Vector2 tmp = new Vector2(), tmp2 = new Vector2();

    private final long id;

    private float angularVelocity;
    private long updateTime;

    public void setPosition(float x, float y) {
        if (!isVisible()) {
            setVisible(true);
        }
        super.setPosition(x, y);
    }

    public long getId() {
        return id;
    }

    public void setTargetPos(float x, float y) {
        targetPos.set(x, y);
    }

    public void clearTargetPos() {
        targetPos.set(Float.NaN, Float.NaN);
    }

    public Vector2 getTargetPos() {
        return targetPos;
    }

    public void getCenterPos(Vector2 pos) {
        pos.set(getWidth() / 2f, getHeight() / 2f);
        pos.rotate(getRotation());
        pos.add(getX(), getY());
    }

    public void getOriginPos(Vector2 pos) {
        pos.set(getOriginX(), getOriginY());
        pos.rotate(getRotation());
        pos.add(getX(), getY());
    }

    public float getLeftX() {
        getCenterPos(tmp);
        return tmp.x - this.getWidth() / 2f;
    }

    public void setVelocity(float velX, float velY, float angularVelocity, long timeNow) {
        velocity.set(velX, velY);

        this.angularVelocity = angularVelocity;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public void getHeadingVector(Vector2 vec, float angleDeg, float length) {
        float angleRad = angleDeg * MathUtils.degreesToRadians;
        vec.set(MathUtils.cos(angleRad) * length, MathUtils.sin(angleRad) * length);
    }

    public float getAngularVelocity() {
        return angularVelocity;
    }

    public void setAngularVelocity(float angularVelocity) {
        this.angularVelocity = angularVelocity;
    }

    public boolean hasTargetPos() {
        return !Float.isNaN(targetPos.x);
    }

    public void updatePos(long timeNow) {
        final float delta = (timeNow - updateTime) / 1000f;
        tmp2.set(lastServerPosition.x + delta * velocity.x,
                 lastServerPosition.y + delta * velocity.y);
        tmp.set(getX(), getY());
        float dst = tmp.dst(tmp2);

        if (dst > MAX_DST_CHANGE_PER_FRAME) {
            if (dst > MIN_TELEPORT_DST) {
                // if the dst is too high, i.e. it would take over 1sec to catch up, just teleport
                tmp.set(tmp2);
                log("teleported because " + dst + " > " + MIN_TELEPORT_DST);
            }
            else {
                tmp.lerp(tmp2, MAX_DST_CHANGE_PER_FRAME / dst);
                // log("catching up " + MAX_DST_CHANGE_PER_FRAME / dst);
            }
        }
        else {
            // log("moved " + dst);
            tmp.set(tmp2);
        }
        setPosition(tmp.x, tmp.y);
        setRotation(lastServerRotation + delta * angularVelocity);
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public void setImpulse(Vector2 i) {
        impulse.set(i);
    }

    public Vector2 getImpulse() {
        return impulse;
    }

    public TurningState getTurningState() {
        return turningState;
    }

    public void setTurningState(TurningState turningState) {
        this.turningState = turningState;
    }

    /**
     *
     * 0     1
     *  +---+
     *  |   |
     *  |   |
     *  +---+
     * 3     2
     *
     * @param corners
     */
    public void getBoundingBox(Vector2 [] corners) {
        getCenterPos(corners[0]);
        for (int i=1; i < 4; i++) {
            corners[i].set(corners[i-1]);
        }

        float halfWidth = getWidth() / 2f;
        float halfHeight = getHeight() / 2f;
        corners[0].x -= halfWidth;
        corners[0].y += halfHeight;

        corners[1].x += halfWidth;
        corners[1].y += halfHeight;

        corners[2].x += halfWidth;
        corners[2].y -= halfHeight;

        corners[3].x -= halfWidth;
        corners[3].y -= halfHeight;
    }

    public void getBoundingBox(Rectangle r) {
        r.setPosition(getX(), getY());
        r.setSize(getWidth(), getHeight());
    }


    public Player getOwner() {
        return owner;
    }

    public String toString() {
        return "ClientShip " + getId();
    }

    public void setLastFiringTime(long lastFiringTime) {
        this.lastFiringTime = lastFiringTime;
    }

    public float getHealth() {
        return health;
    }

    public ShipStats getStats() {
        return stats;
    }

    public Image getClickRegionImage() {
        return clickRegionImage;
    }

    public static ClientShip getShip(Actor image) {
        return (ClientShip) image.getParent();
    }

    public boolean hasDirectionTarget() {
        return !Float.isNaN(targetDirection);
    }

    public boolean isOrbitCW() {
        return orbitCW;
    }

    public ClientShip getOrbitShip() {
        return orbitShip;
    }

    public float getOrbitDst2() {
        return orbitDst2;
    }

    public float getMaxOrbitalVelocityInTheory(float orbitDst) {
        float maxAcceleration = stats.getMaxLinearForceLeft() / stats.getMass();



        // todo: figure out if turning and going forward gives better limit on the force
        float maxVelocity = (float) Math.sqrt(maxAcceleration * Math.sqrt(orbitDst2));
        maxVelocity = MathUtils.clamp(maxVelocity, 0, stats.getMaxLinearVelocity());
        return maxVelocity;
    }

    public float getMaxOrbitalVelocity() {
        float maxAcceleration = stats.getMaxLinearForceLeft() / stats.getMass();
        // todo: figure out if turning and going forward gives better limit on the force
        float maxVelocity = (float) Math.sqrt(maxAcceleration * Math.sqrt(orbitDst2));
        maxVelocity = MathUtils.clamp(maxVelocity, 0, stats.getMaxLinearVelocity());
        return maxVelocity;
    }

    public void setLastServerPosition(float x, float y, float rotation) {
        lastServerPosition.set(x, y);
        lastServerRotation = rotation;
    }

    public Array<PositionProjection> getProjectedPositions() {
        return projectedPositions;
    }

    public void setSelected(boolean h) {
        baseImage.setVisible(!h);
        hiliteImage.setVisible(h);
    }

    public Color getCurrentColor() {
        if (hiliteImage.isVisible()) {
            return Assets.getHiliteColor(owner);
        }
        else {
            return Assets.getBasicColor(owner);
        }
    }

    public ShipShootingAIDecisionTree.Decision getDecisionTreeRoot() {
        return decisionTreeRoot;
    }

    public void setDecisionTreeRoot(ShipShootingAIDecisionTree.Decision decisionTreeRoot) {
        this.decisionTreeRoot = decisionTreeRoot;
    }

    public boolean isDecisionTreeDirty() {
        return decisionTreeDirty;
    }

    public void setDecisionTreeDirty(boolean decisionTreeDirty) {
        this.decisionTreeDirty = decisionTreeDirty;
    }

    // in form of type + id, for example "frigate 42"
    public String getDescription() {
        return stats.getType().toString() + " " + id;
    }

    public String getDescriptionWithOwner() {
        return owner.getName() + "s " + getDescription();
    }
}
