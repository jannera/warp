package com.rasanenj.warp.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.rasanenj.warp.Assets;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class ClientShip extends Image {
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

    private static final float MAX_ANGULAR_CHANGE = 5f;
    public static final float MAX_SPEED = 5f;

    private final float maxAngularForce = 1000;

    private final long id;

    private final float mass, inertia;
    private float angularVelocity;
    private long updateTime;
    private float angularAcceleration;
    private boolean stoppingImpulseSent = false;

    public ClientShip(long id, float width, float height, float mass, float inertia) {
        super(Assets.shipTexture);
        this.id = id;
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
    }

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

    public float maxAngularAcceleration() {
        return 12f;
    }

    /**
     * in degrees per second
      */
    public float getMaxAngularVelocity() {
        return 35f;
    }

    public void setVelocity(float velX, float velY) {
        velocity.set(velX, velY);
    }

    public float getMass() {
        return mass;
    }

    public float getMaxImpulse() {
        return getMass() * getMaxSpeed() * 0.025f;
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

    public float getMaxSpeed() {
        return 2;
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
        impulse.nor();
        impulse.scl(i.len() / getMaxImpulse() * 0.1f); // TODO: fix the scaling once the maximum forces of ship are defined in vectors
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

    public void setStoppingImpulseSent(boolean stoppingImpulseSent) {
        this.stoppingImpulseSent = stoppingImpulseSent;
    }

    public boolean isStoppingImpulseSent() {
        return stoppingImpulseSent;
    }

    public TurningState getTurningState() {
        return turningState;
    }

    public void setTurningState(TurningState turningState) {
        this.turningState = turningState;
    }
}
