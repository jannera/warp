package com.rasanenj.warp.messaging;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.rasanenj.warp.Settings;

import java.nio.ByteBuffer;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class ShipPhysicsMessage extends EntityMessage {
    private final float x,y, velX, velY, angle, angularVelocity;
    private final Vector2[] vertices;
    private boolean teleport;

    public ShipPhysicsMessage(long id, Vector2 pos, float angle, Body body, boolean teleport) {
        super(id);
        Vector2 linearVelocity = body.getLinearVelocity();
        this.x = pos.x;
        this.y = pos.y;
        this.velX = linearVelocity.x;
        this.velY = linearVelocity.y;
        this.angle = angle;
        this.angularVelocity = body.getAngularVelocity();
        this.teleport = teleport;

        if (!Settings.renderPhysicsFixtures) {
            vertices = new Vector2[0];
            return;
        }
        // creates a Vector2 for every point of the fixture
        Array<Fixture> fixtures = body.getFixtureList();
        int count = 0;
        for (Fixture f : fixtures) {
            count += ((PolygonShape) f.getShape()).getVertexCount();
        }
        this.vertices = new Vector2[count];
        for (Fixture f : fixtures) {
            PolygonShape shape =  (PolygonShape) f.getShape();
            for (int i=0; i < shape.getVertexCount(); i++) {
                vertices[i] = new Vector2();
                shape.getVertex(i, vertices[i]);
                vertices[i].rotate(body.getAngle() * MathUtils.radiansToDegrees);
                vertices[i].add(body.getPosition());
            }
        }
    }

    public ShipPhysicsMessage(ByteBuffer b) {
        super(b.getLong());
        this.x = b.getFloat();
        this.y = b.getFloat();
        this.velX = b.getFloat();
        this.velY = b.getFloat();
        this.angle = b.getFloat();
        this.angularVelocity = b.getFloat();
        this.teleport = Message.getBoolean(b);
        this.vertices = Message.getVectors(b);
    }

    @Override
    public MessageType getType() {
        return MessageType.UPDATE_SHIP_PHYSICS;
    }

    @Override
    public byte[] encode() {
        ByteBuffer b = create(Float.SIZE/8 * 6 + Long.SIZE/8 +
                Short.SIZE/8 + Message.getBytesForVectors(vertices));
        b.putLong(id).putFloat(x).putFloat(y).putFloat(velX).putFloat(velY).putFloat(angle)
        .putFloat(angularVelocity);
        Message.putBoolean(b, teleport);
        Message.putVectors(b, vertices);
        return b.array();
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getVelX() {
        return velX;
    }

    public float getVelY() {
        return velY;
    }

    public float getAngle() {
        return angle;
    }

    public float getAngularVelocity() {
        return angularVelocity;
    }

    public Vector2[] getVertices() {
        return vertices;
    }

    public boolean isTeleport() {
        return teleport;
    }
}
