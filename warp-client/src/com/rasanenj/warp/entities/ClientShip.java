package com.rasanenj.warp.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
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
    private static final Texture arrowTexture = new Texture(Gdx.files.internal("data/arrow.png"));
    private final Image targetImg, headingArrow, velocityArrow, impulseArrow;
    Vector2 targetPos = new Vector2();
    Vector2 velocity = new Vector2();

    Vector2 tmp = new Vector2();

    private static final float MAX_ANGULAR_CHANGE = 5f;
    public static final float MAX_SPEED = 5f;

    private final float maxAngularForce = 1000;

    private final long id;

    private final float mass;
    private float angularVelocity;

    public ClientShip(long id, float width, float height, float mass) {
        super(shipTexture);
        this.id = id;
        this.setWidth(width);
        this.setHeight(height);
        setVisible(false);
        this.targetImg = new Image(targetTexture);
        this.headingArrow = new Image(arrowTexture);
        this.velocityArrow = new Image(arrowTexture);
        this.impulseArrow = new Image(arrowTexture);
        this.headingArrow.setVisible(true);
        this.velocityArrow.setVisible(false);
        this.impulseArrow.setVisible(false);
        this.targetImg.setVisible(false);
        this.targetImg.setBounds(0, 0, 24, 24);
        targetPos.set(Float.NaN, Float.NaN);
        this.headingArrow.setColor(0f, 191f/255, 1f, 1f);
        this.mass = mass;

        getCenterPos(tmp);
        setOrigin(tmp.x - headingArrow.getWidth() / 2f, tmp.y);
    }

    public void updateArrows() {
        getCenterPos(tmp);
        float angle = getRotation();
        headingArrow.setPosition(tmp.x - headingArrow.getWidth() / 2f, tmp.y);
        headingArrow.setRotation(angle);
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
        stage.addActor(headingArrow);
        stage.addActor(impulseArrow);
        stage.addActor(velocityArrow);
    }

    public void setTargetPos(float x, float y) {
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
}
