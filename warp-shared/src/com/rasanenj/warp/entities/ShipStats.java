package com.rasanenj.warp.entities;

import com.rasanenj.warp.messaging.Message;

import java.nio.ByteBuffer;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class ShipStats {
    private final float signatureResolution;
    private final float weaponTracking, weaponSignatureRadius, weaponOptimal, weaponFalloff, weaponDamage, weaponCooldown;
    private float maxLinearVelocity;
    private final float maxHealth;
    private float maxAcceleration;
    private final float cost;
    private final float width, height;
    private final Shiptype type;
    private final OrbitVelocities orbitVelocities = new OrbitVelocities();

    @Override
    public int hashCode() {
        int result = (signatureResolution != +0.0f ? Float.floatToIntBits(signatureResolution) : 0);
        result = 31 * result + (weaponTracking != +0.0f ? Float.floatToIntBits(weaponTracking) : 0);
        result = 31 * result + (weaponSignatureRadius != +0.0f ? Float.floatToIntBits(weaponSignatureRadius) : 0);
        result = 31 * result + (weaponOptimal != +0.0f ? Float.floatToIntBits(weaponOptimal) : 0);
        result = 31 * result + (weaponFalloff != +0.0f ? Float.floatToIntBits(weaponFalloff) : 0);
        result = 31 * result + (weaponDamage != +0.0f ? Float.floatToIntBits(weaponDamage) : 0);
        result = 31 * result + (weaponCooldown != +0.0f ? Float.floatToIntBits(weaponCooldown) : 0);
        result = 31 * result + (maxLinearVelocity != +0.0f ? Float.floatToIntBits(maxLinearVelocity) : 0);
        result = 31 * result + (maxHealth != +0.0f ? Float.floatToIntBits(maxHealth) : 0);
        result = 31 * result + (maxAcceleration != +0.0f ? Float.floatToIntBits(maxAcceleration) : 0);
        result = 31 * result + (cost != +0.0f ? Float.floatToIntBits(cost) : 0);
        result = 31 * result + (width != +0.0f ? Float.floatToIntBits(width) : 0);
        result = 31 * result + (height != +0.0f ? Float.floatToIntBits(height) : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (maxAngularAcceleration != +0.0f ? Float.floatToIntBits(maxAngularAcceleration) : 0);
        result = 31 * result + (maxAngularVelocity != +0.0f ? Float.floatToIntBits(maxAngularVelocity) : 0);
        result = 31 * result + (mass != +0.0f ? Float.floatToIntBits(mass) : 0);
        result = 31 * result + (inertia != +0.0f ? Float.floatToIntBits(inertia) : 0);
        result = 31 * result + (maxLinearForceRight != +0.0f ? Float.floatToIntBits(maxLinearForceRight) : 0);
        result = 31 * result + (maxLinearForceForward != +0.0f ? Float.floatToIntBits(maxLinearForceForward) : 0);
        result = 31 * result + (maxLinearForceBackward != +0.0f ? Float.floatToIntBits(maxLinearForceBackward) : 0);
        result = 31 * result + (maxLinearForceLeft != +0.0f ? Float.floatToIntBits(maxLinearForceLeft) : 0);
        return result;
    }

    public enum Shiptype {
        FRIGATE, CRUISER, BATTLESHIP;

        public void encode(ByteBuffer b) {
            b.putInt(this.ordinal());
        }

        public static Shiptype decode(ByteBuffer b) {
            int i = b.getInt();

            if (i==FRIGATE.ordinal()) {
                return FRIGATE;
            }
            if (i==CRUISER.ordinal()) {
                return CRUISER;
            }
            if (i==BATTLESHIP.ordinal()) {
                return BATTLESHIP;
            }
            return null;
        }
    }

    public ShipStats(float mass, float inertia,
                     float maxLinearForceForward, float maxLinearForceBackward,
                     float maxLinearForceLeft, float maxLinearForceRight,
                     float maxHealth, float maxVelocity, float maxAngularVelocity,
                     float maxAngularAcceleration, float signatureResolution,
                     float weaponTracking, float weaponSignatureRadius,
                     float weaponOptimal, float weaponFalloff,
                     float weaponDamage, float weaponCooldown, float maxAcceleration,
                     float cost,
                     float width, float height,
                     Shiptype type) {
        this.mass = mass;
        this.inertia = inertia;
        this.maxLinearForceForward = maxLinearForceForward;
        this.maxLinearForceBackward = maxLinearForceBackward;
        this.maxLinearForceLeft = maxLinearForceLeft;
        this.maxLinearForceRight = maxLinearForceRight;
        this.maxHealth = maxHealth;
        this.maxLinearVelocity = maxVelocity;
        this.maxAngularVelocity = maxAngularVelocity;
        this.maxAngularAcceleration = maxAngularAcceleration;
        this.signatureResolution = signatureResolution;
        this.weaponTracking = weaponTracking;
        this.weaponSignatureRadius = weaponSignatureRadius;
        this.weaponOptimal = weaponOptimal;
        this.weaponFalloff = weaponFalloff;
        this.weaponDamage = weaponDamage;
        this.weaponCooldown = weaponCooldown;
        this.maxAcceleration = maxAcceleration;
        this.cost = cost;
        this.width = width;
        this.height = height;
        this.type = type;
    }

    public ShipStats(ByteBuffer b) {
        this.mass = b.getFloat();
        this.inertia = b.getFloat();
        this.maxLinearForceForward = b.getFloat();
        this.maxLinearForceBackward = b.getFloat();
        this.maxLinearForceLeft = b.getFloat();
        this.maxLinearForceRight = b.getFloat();
        this.maxHealth = b.getFloat();
        this.maxLinearVelocity = b.getFloat();
        this.maxAngularVelocity = b.getFloat();
        this.maxAngularAcceleration = b.getFloat();
        this.signatureResolution = b.getFloat();
        this.weaponTracking = b.getFloat();
        this.weaponSignatureRadius = b.getFloat();
        this.weaponOptimal = b.getFloat();
        this.weaponFalloff = b.getFloat();
        this.weaponDamage = b.getFloat();
        this.weaponCooldown = b.getFloat();
        this.maxAcceleration = b.getFloat();
        this.cost = b.getFloat();
        this.width = b.getFloat();
        this.height = b.getFloat();
        this.type = Shiptype.decode(b);
    }

    public void encode(ByteBuffer b) {
        Message.putFloats(b, mass, inertia, maxLinearForceForward, maxLinearForceBackward,
                maxLinearForceLeft, maxLinearForceRight, maxHealth, maxLinearVelocity,
                maxAngularVelocity, maxAngularAcceleration, signatureResolution,
                weaponTracking, weaponSignatureRadius, weaponOptimal, weaponFalloff,
                weaponDamage, weaponCooldown, maxAcceleration, cost, width, height);
        type.encode(b);
    }

    public static int getLengthInBytes() {
        return Float.SIZE/8 * 21 + Integer.SIZE/8;
    }

    public float getSignatureResolution() {
        return signatureResolution;
    }

    public float getWeaponTracking() {
        return weaponTracking;
    }

    public float getWeaponSignatureRadius() {
        return weaponSignatureRadius;
    }

    public float getWeaponOptimal() {
        return weaponOptimal;
    }

    public float getWeaponFalloff() {
        return weaponFalloff;
    }

    public float getMaxLinearVelocity() {
        return maxLinearVelocity;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public float getMaxAngularAcceleration() {
        return maxAngularAcceleration;
    }

    public float getMass() {
        return mass;
    }

    public float getInertia() {
        return inertia;
    }

    /**
     * in degrees per second
     */
    public float getMaxAngularVelocity() {
        return maxAngularVelocity;
    }

    private float maxAngularAcceleration;
    private float maxAngularVelocity;
    private float mass, inertia;

    private float maxLinearForceRight, maxLinearForceForward, maxLinearForceBackward, maxLinearForceLeft;

    public void scaleForces(float scale) {
        this.maxLinearForceForward *= scale;
        this.maxLinearForceBackward *= scale;
        this.maxLinearForceLeft *= scale;
        this.maxLinearForceRight *= scale;
    }

    public void setForceLimits(float maxLinearForceForward, float maxLinearForceBackward,
                               float maxLinearForceLeft, float maxLinearForceRight) {
        this.maxLinearForceLeft = maxLinearForceLeft;
        this.maxLinearForceBackward = maxLinearForceBackward;
        this.maxLinearForceForward = maxLinearForceForward;
        this.maxLinearForceRight = maxLinearForceRight;
    }

    public float getMaxLinearForceRight() {
        return maxLinearForceRight;
    }

    public float getMaxLinearForceForward() {
        return maxLinearForceForward;
    }

    public float getMaxLinearForceBackward() {
        return maxLinearForceBackward;
    }

    public float getMaxLinearForceLeft() {
        return maxLinearForceLeft;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public void setInertia(float inertia) {
        this.inertia = inertia;
    }

    public float getWeaponDamage() {
        return weaponDamage;
    }

    public float getWeaponCooldown() {
        return weaponCooldown;
    }

    public float getMaxAcceleration() {
        return maxAcceleration;
    }

    public float getCost() {
        return cost;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public Shiptype getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShipStats shipStats = (ShipStats) o;

        if (Float.compare(shipStats.cost, cost) != 0) return false;
        if (Float.compare(shipStats.height, height) != 0) return false;
        if (Float.compare(shipStats.inertia, inertia) != 0) return false;
        if (Float.compare(shipStats.mass, mass) != 0) return false;
        if (Float.compare(shipStats.maxAcceleration, maxAcceleration) != 0) return false;
        if (Float.compare(shipStats.maxAngularAcceleration, maxAngularAcceleration) != 0) return false;
        if (Float.compare(shipStats.maxAngularVelocity, maxAngularVelocity) != 0) return false;
        if (Float.compare(shipStats.maxHealth, maxHealth) != 0) return false;
        if (Float.compare(shipStats.maxLinearForceBackward, maxLinearForceBackward) != 0) return false;
        if (Float.compare(shipStats.maxLinearForceForward, maxLinearForceForward) != 0) return false;
        if (Float.compare(shipStats.maxLinearForceLeft, maxLinearForceLeft) != 0) return false;
        if (Float.compare(shipStats.maxLinearForceRight, maxLinearForceRight) != 0) return false;
        if (Float.compare(shipStats.maxLinearVelocity, maxLinearVelocity) != 0) return false;
        if (Float.compare(shipStats.signatureResolution, signatureResolution) != 0) return false;
        if (Float.compare(shipStats.weaponCooldown, weaponCooldown) != 0) return false;
        if (Float.compare(shipStats.weaponDamage, weaponDamage) != 0) return false;
        if (Float.compare(shipStats.weaponFalloff, weaponFalloff) != 0) return false;
        if (Float.compare(shipStats.weaponOptimal, weaponOptimal) != 0) return false;
        if (Float.compare(shipStats.weaponSignatureRadius, weaponSignatureRadius) != 0) return false;
        if (Float.compare(shipStats.weaponTracking, weaponTracking) != 0) return false;
        if (Float.compare(shipStats.width, width) != 0) return false;
        if (!type.equals(shipStats.type)) return false;

        return true;
    }

    public OrbitVelocities getOrbitVelocities() {
        return orbitVelocities;
    }
}
