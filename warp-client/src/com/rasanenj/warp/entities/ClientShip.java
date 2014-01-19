package com.rasanenj.warp.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.rasanenj.warp.Assets;
import com.rasanenj.warp.messaging.Player;
import com.rasanenj.warp.systems.ShipSteering;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class ClientShip extends Group {
    private final Player owner;
    private ClientShip firingTarget;
    private long lastFiringTime = 0;
    private float health;

    private static long lastid = 0;

    private final ShipStats stats;

    private final Image image, clickRegionImage;

    private static final float CLICKREGION_MULTIPLIER = 4f; // how many times bigger are should work as clicking area around the ship
    private Vector2[] vertices;
    private String text = "";
    private float targetDirection = Float.NaN;
    private boolean orbitCW;
    private ClientShip orbitShip;
    private float orbitDst2;
    private Color circled;

    public ClientShip(long id, Player owner, ShipStats stats) {
        this.image = new Image(Assets.shipTexture);
        this.clickRegionImage = new Image(Assets.shipTexture);
        this.clickRegionImage.setColor(1, 1, 1, 0);
        this.clickRegionImage.setVisible(true);
        float width = stats.getWidth();
        float height = stats.getHeight();
        clickRegionImage.setWidth(width * CLICKREGION_MULTIPLIER);
        clickRegionImage.setHeight(height * CLICKREGION_MULTIPLIER);
        addActor(image);
        addActor(clickRegionImage);
        this.stats = stats;
        lastid = id;
        this.id = id;
        this.owner = owner;
        this.setWidth(width);
        this.setHeight(height);
        setVisible(false);
        image.setWidth(width);
        image.setHeight(height);

        accRefresh = 0;

        // make the hovering and non-hovering ships overlap on the center
        clickRegionImage.setPosition(image.getWidth() / 2f - clickRegionImage.getWidth() / 2f,
                image.getHeight() / 2f - clickRegionImage.getHeight() / 2f);

        this.stats.scaleForces(ShipSteering.STEP_LENGTH);
        this.health = stats.getMaxHealth();

        this.clearAllSteering();
    }

    private float brakingLeft;
    private Vector2 impulseIdeal = new Vector2();

    public void setBrakingLeft(float brakingLeft) {
        this.brakingLeft = brakingLeft;
    }

    public float getBrakingLeft() {
        return brakingLeft;
    }

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

    public void setFiringTarget(ClientShip firingTarget) {
        this.firingTarget = firingTarget;
    }

    public ClientShip getFiringTarget() {
        return firingTarget;
    }

    public boolean canFire() {
        if (System.currentTimeMillis() - lastFiringTime > stats.getWeaponCooldown() * 1000) {
            return true;
        }
        return false;
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

    public enum TurningState {
        FULL_SPEED, BRAKING, DONE_BRAKING;
    }

    TurningState turningState;

    Vector2 targetPos = new Vector2();
    Vector2 velocity = new Vector2();
    Vector2 impulse = new Vector2();

    Vector2 tmp = new Vector2();

    private static final float ACC_FREQ = 1f /10f;
    private float oldAngularVelocity = 0;
    private float oldVelocity = 0;
    private long accRefresh = 0;

    private final long id;

    private float angularVelocity;
    private long updateTime;
    private float angularAcceleration;
    private float acceleration;

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

    public void setVelocity(float velX, float velY, float angularVelocity, long timeNow) {
        velocity.set(velX, velY);

        final float delta = (timeNow - accRefresh) / 1000f;
        if (delta > ACC_FREQ) {
            if (getId() == lastid) {
                //log("diff " + (angularVelocity - this.oldAngularVelocity));
                // log(angularVelocity + " vs " + oldAngularVelocity);
            }
            // TODO: these could be taken as a sum of last n updates, stored in an array
            // TODO: these are for some reason totally wrong. compare to VelocityPrinter
            this.angularAcceleration =  (angularVelocity - this.oldAngularVelocity) / delta;
            this.acceleration = (velocity.len() - oldVelocity) / delta;
            accRefresh = timeNow;
            oldAngularVelocity = angularVelocity;
        }

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

    public boolean hasTargetPos() {
        return !Float.isNaN(targetPos.x);
    }

    public void updatePos(long timeNow) {
        final float delta = (timeNow - updateTime) / 1000f;
        log("delta:" + delta);
        setPosition(getX() + delta * velocity.x, getY() + delta * velocity.y);
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

    public float getAngularAcceleration() {
        return angularAcceleration;
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

    public float getAcceleration() {
        return acceleration;
    }

    public ShipStats getStats() {
        return stats;
    }

    public Image getImage() {
        return image;
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
}
