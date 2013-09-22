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
public class ServerShip extends Ship {
    private ServerPlayer player;
    private final Body body;
    private static final BodyDef bodyDef = new BodyDef();
    private static final PolygonShape polygonShape = new PolygonShape();
    private static final float DENSITY = 1f;

    static {
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.active = true;
        bodyDef.linearDamping = 0f;
        bodyDef.angularDamping = 0f;
    }

    public ServerShip(World world, float x, float y, float angleRad, float width, float height, ServerPlayer player) {
        super(x, y, width, height);
        body = world.createBody(bodyDef);
        body.setTransform(x, y, angleRad);
        polygonShape.setAsBox(width, height);
        body.createFixture(polygonShape, DENSITY);
        this.player = player;
    }

    public Body getBody() {
        return body;
    }

    public float getMass() {
        return body.getMass();
    }

    public Vector2 getEngineLocation() {
        return body.getWorldCenter(); // TODO FIX
    }

    public ServerPlayer getPlayer() {
        return player;
    }
}
