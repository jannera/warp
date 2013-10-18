package com.rasanenj.warp.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.rasanenj.warp.ServerPlayer;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class ServerShip extends Entity {
    private ServerPlayer player;
    private final Body body;
    private static final BodyDef bodyDef = new BodyDef();
    private static final PolygonShape polygonShape = new PolygonShape();
    private static final float DENSITY = 10f;

    private final Vector2 oldPos = new Vector2();
    private float oldAngle = 0;

    private float width, height;

    private final float maxLinearForceRight, maxLinearForceForward, maxLinearForceBackward, maxLinearForceLeft;
    private final float maxHealth, maxVelocity, maxAngularVelocity, maxAngularAcceleration;

    private float health;

    static {
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.active = true;
        bodyDef.linearDamping = 0f;
        bodyDef.angularDamping = 0f;
    }



    public ServerShip(World world, float x, float y, float angleRad, float width, float height, ServerPlayer player,
                      float acceleration, float maxHealth, float maxVelocity, float maxAngularVelocity,
                      float maxAngularAcceleration) {
        this.width = width;
        this.height = height;
        body = world.createBody(bodyDef);
        body.setTransform(x, y, angleRad);
        polygonShape.setAsBox(width, height);
        body.createFixture(polygonShape, DENSITY);
        this.player = player;
        storeOldPosition();

        // F = ma
        float mass = body.getMass();
        this.maxLinearForceForward = mass * acceleration;
        this.maxLinearForceBackward = mass * acceleration / 2f;
        this.maxLinearForceLeft = mass * acceleration / 4f;
        this.maxLinearForceRight = mass * acceleration / 4f;

        this.maxHealth = maxHealth;
        this.maxVelocity = maxVelocity;
        this.maxAngularVelocity = maxAngularVelocity;
        this.maxAngularAcceleration = maxAngularAcceleration;

        this.health = maxHealth;
    }

    public Body getBody() {
        return body;
    }

    public float getMass() {
        return body.getMass();
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public void storeOldPosition() {
        oldAngle = body.getAngle();
        oldPos.set(body.getPosition());
    }

    public void getInterpolatedPosition(Vector2 result, float lerp1, float lerp2) {
        result.set(body.getPosition());
        result.scl(lerp1);
        result.add(oldPos.x * lerp2, oldPos.y * lerp2);
    }

    public float getInterpolatedAngle(float lerp1, float lerp2) {
        return body.getAngle() * lerp1 + oldAngle * lerp2;
    }

    public float getInertia() {
        return body.getInertia();
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getMaxLinearForceLeft() {
        return maxLinearForceLeft;
    }

    public float getMaxLinearForceBackward() {
        return maxLinearForceBackward;
    }

    public float getMaxLinearForceForward() {
        return maxLinearForceForward;
    }

    public float getMaxLinearForceRight() {
        return maxLinearForceRight;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public float getMaxVelocity() {
        return maxVelocity;
    }

    public float getMaxAngularVelocity() {
        return maxAngularVelocity;
    }

    public void reduceHealth(float damage) {
        health -= damage;
    }

    public float getHealth() {
        return health;
    }

    public float getMaxAngularAcceleration() {
        return maxAngularAcceleration;
    }
}
