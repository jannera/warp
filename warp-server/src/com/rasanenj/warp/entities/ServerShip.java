package com.rasanenj.warp.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.rasanenj.warp.ServerPlayer;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class ServerShip extends Entity {
    private final ShipStats stats;
    private ServerPlayer player;
    private final Body body;
    private static final BodyDef bodyDef = new BodyDef();
    private static final PolygonShape polygonShape = new PolygonShape();
    private static final FixtureDef fixtureDef = new FixtureDef();
    private static final float DENSITY = 10f;

    private final Vector2 oldPos = new Vector2();
    private float oldAngle = 0;

    private float width, height;

    private float health;

    static {
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.active = true;
        bodyDef.linearDamping = 0f;
        bodyDef.angularDamping = 0f;

        fixtureDef.density = DENSITY;
        fixtureDef.friction = 0.9f;
        fixtureDef.isSensor = false;
        fixtureDef.restitution = 0.01f;
        fixtureDef.shape = polygonShape;
    }



    public ServerShip(World world, float x, float y, float angleRad, float width, float height, ServerPlayer player,
                      ShipStats stats) {
        this.width = width;
        this.height = height;
        body = world.createBody(bodyDef);
        body.setTransform(x, y, angleRad);
        Vector2 localCenter = new Vector2(width/2f, height/2f);
        polygonShape.setAsBox(width, height, localCenter, 0f);
        body.createFixture(fixtureDef);
        this.player = player;
        storeOldPosition();

        // F = ma
        float mass = body.getMass();

        this.stats = stats;
        float acceleration = stats.getMaxAcceleration();
        this.stats.setForceLimits(mass * acceleration, mass * acceleration / 2f,
                mass * acceleration / 4f, mass * acceleration / 4f);
        this.stats.setMass(mass);
        this.stats.setInertia(body.getInertia());

        this.health = stats.getMaxHealth();
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
        oldAngle = body.getAngle() * MathUtils.radiansToDegrees;
        oldPos.set(body.getPosition());
    }

    public void getInterpolatedPosition(Vector2 result, float lerp1, float lerp2) {
        result.set(body.getPosition());
        result.scl(lerp1);
        result.add(oldPos.x * lerp2, oldPos.y * lerp2);
    }

    public float getInterpolatedAngle(float lerp1, float lerp2) {
        return body.getAngle() * MathUtils.radiansToDegrees * lerp1 + oldAngle * lerp2;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public void reduceHealth(float damage) {
        health -= damage;
    }

    public float getHealth() {
        return health;
    }

    public ShipStats getStats() {
        return stats;
    }
}
