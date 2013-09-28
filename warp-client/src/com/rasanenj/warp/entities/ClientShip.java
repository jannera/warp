package com.rasanenj.warp.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class ClientShip extends Image {
    private static final Texture shipTexture = new Texture(Gdx.files.internal("data/grey_block.png"));
    private static final Texture targetTexture = new Texture(Gdx.files.internal("data/target_rectangle.png"));
    private final Image targetImg;
    Vector2 targetPos = new Vector2();
    Vector2 velocity = new Vector2();
    Vector2 impulse = new Vector2();

    Vector2 tmp = new Vector2();

    private static final float MAX_ANGULAR_CHANGE = 5f;
    public static final float MAX_SPEED = 5f;

    private final float maxAngularForce = 1000;

    private final long id;

    private final float mass;
    private float angularVelocity;
    private long updateTime;

    public ClientShip(long id, float width, float height, float mass) {
        super(shipTexture);
        this.id = id;
        this.setWidth(width);
        this.setHeight(height);
        setVisible(false);
        this.targetImg = new Image(targetTexture);
        float tgtImgBound = Math.max(width, height);
        this.targetImg.setBounds(0, 0, tgtImgBound, tgtImgBound);
        clearTargetPos();

        this.mass = mass;

        getCenterPos(tmp);
        setOrigin(tmp.x, tmp.y);
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

    public float getMaxAngularForce() {
        return mass * MAX_ANGULAR_CHANGE;
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

    public void setAngularVelocity(float angularVelocity) {
        this.angularVelocity = angularVelocity;
    }

    public float getAngularVelocity() {
        return angularVelocity;
    }

    public float getMaxSpeed() {
        return 10;
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
}
