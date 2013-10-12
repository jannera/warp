package com.rasanenj.warp.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.rasanenj.warp.Assets;
import com.rasanenj.warp.messaging.Player;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class ClientShip extends Image {
    private final Player owner;

    public ClientShip(long id, Player owner, float width, float height, float mass, float inertia,
                      float maxLinearForceForward, float maxLinearForceBackward,
                      float maxLinearForceLeft, float maxLinearForceRight,
                      float maxHealth, float maxVelocity, float maxAngularVelocity) {
        super(Assets.shipTexture);
        this.id = id;
        this.owner = owner;
        this.setWidth(width);
        this.setHeight(height);
        setVisible(false);
        this.targetImg = new Image(Assets.targetTexture);
        float tgtImgBound = Math.max(width, height);
        this.targetImg.setBounds(0, 0, tgtImgBound, tgtImgBound);
        clearTargetPos();

        this.mass = mass;
        this.inertia = inertia;

        getCenterPos(tmp);
        setOrigin(tmp.x, tmp.y);
        accRefresh = 0;

        this.maxLinearForceForward = maxLinearForceForward;
        this.maxLinearForceBackward = maxLinearForceBackward;
        this.maxLinearForceLeft = maxLinearForceLeft;
        this.maxLinearForceRight = maxLinearForceRight;

        this.maxHealth = maxHealth;
        this.maxLinearVelocity = maxVelocity;
        this.maxAngularVelocity = maxAngularVelocity;
    }

    private final float maxLinearVelocity;
    private final float maxHealth;
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
        corners[0].x = maxLinearForceForward;
        corners[0].y = maxLinearForceLeft;

        corners[1].x = maxLinearForceForward;
        corners[1].y = -maxLinearForceRight;

        corners[2].x = -maxLinearForceBackward;
        corners[2].y = -maxLinearForceRight;

        corners[3].x = -maxLinearForceBackward;
        corners[3].y = maxLinearForceLeft;

        getCenterPos(tmp);
        for (int i=0; i < 4; i++) {
            corners[i].rotate(getRotation());
            corners[i].add(tmp);
        }
    }

    public enum TurningState {
        FULL_SPEED, BRAKING, DONE_BRAKING;
    }

    TurningState turningState;

    private final Image targetImg;
    Vector2 targetPos = new Vector2();
    Vector2 velocity = new Vector2();
    Vector2 impulse = new Vector2();

    Vector2 tmp = new Vector2();

    private static final float ACC_FREQ = 1f /10f;
    private float oldAngularVelocity = 0;
    private long accRefresh = 0;

    private final long id;

    private final float mass, inertia;
    private float angularVelocity;
    private long updateTime;
    private float angularAcceleration;

    public void setPosition(float x, float y) {
        if (!isVisible()) {
            setVisible(true);
        }
        super.setPosition(x, y);
    }

    public long getId() {
        return id;
    }

    public void attach(Stage stage) {
        stage.addActor(targetImg);
    }

    public void setTargetPos(float x, float y) {
        log(x + ", " + y);
        targetPos.set(x, y);
        targetImg.setPosition(x - targetImg.getHeight()/2f, y - targetImg.getWidth()/2f);
        targetImg.setVisible(true);
    }

    public Vector2 getTargetPos() {
        return targetPos;
    }

    public void getCenterPos(Vector2 pos) {
        pos.set(getX(), getY());
        pos.add(getWidth() / 2f, getHeight() / 2f);
    }

    public float getMaxAngularAcceleration() {
        return maxAngularAcceleration;
    }

    /**
     * in degrees per second
      */
    public float getMaxAngularVelocity() {
        return maxAngularVelocity;
    }

    public void setVelocity(float velX, float velY) {
        velocity.set(velX, velY);
    }

    public float getMass() {
        return mass;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public void getHeadingVector(Vector2 vec, float angleDeg, float length) {
        float angleRad = angleDeg * MathUtils.degreesToRadians;
        vec.set(MathUtils.cos(angleRad) * length, MathUtils.sin(angleRad) * length);
    }

    public void setAngularVelocity(float angularVelocity, long timeNow) {
        final float delta = (timeNow - accRefresh) / 1000f;
        if (delta > ACC_FREQ) {
            // log("diff " + (angularVelocity - this.oldAngularVelocity));
            this.angularAcceleration =  (angularVelocity - this.oldAngularVelocity) / delta;
            accRefresh = timeNow;
            oldAngularVelocity = angularVelocity;
        }
        this.angularVelocity = angularVelocity;
    }

    public float getAngularVelocity() {
        return angularVelocity;
    }

    public void clearTargetPos() {
        targetPos.set(Float.NaN, Float.NaN);
        this.targetImg.setVisible(false);
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

    public float getInertia() {
        return inertia;
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


    private final float maxAngularAcceleration = 12f; // TODO: create all this when the ship is created, given from Server
    private final float maxAngularVelocity;
    private final float maxLinearForceRight, maxLinearForceForward, maxLinearForceBackward, maxLinearForceLeft;

    public float getMaxLinearVelocity() {
        return maxLinearVelocity;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public long getOwnerId() {
        return owner.getId();
    }

    public Player getOwner() {
        return owner;
    }
}
