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
    private final float maxLinearVelocity;
    private final float maxHealth;
    private final float maxAcceleration;
    private final float cost;
    private final float width, height;

    public ShipStats(float mass, float inertia,
                     float maxLinearForceForward, float maxLinearForceBackward,
                     float maxLinearForceLeft, float maxLinearForceRight,
                     float maxHealth, float maxVelocity, float maxAngularVelocity,
                     float maxAngularAcceleration, float signatureResolution,
                     float weaponTracking, float weaponSignatureRadius,
                     float weaponOptimal, float weaponFalloff,
                     float weaponDamage, float weaponCooldown, float maxAcceleration,
                     float cost,
                     float width, float height) {
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
    }

    public void encode(ByteBuffer b) {
        Message.putFloats(b, mass, inertia, maxLinearForceForward, maxLinearForceBackward,
                maxLinearForceLeft, maxLinearForceRight, maxHealth, maxLinearVelocity,
                maxAngularVelocity, maxAngularAcceleration, signatureResolution,
                weaponTracking, weaponSignatureRadius, weaponOptimal, weaponFalloff,
                weaponDamage, weaponCooldown, maxAcceleration, cost, width, height);
    }

    public static int getLengthInBytes() {
        return Float.SIZE/8 * 21;
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

    private final float maxAngularAcceleration;
    private final float maxAngularVelocity;
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
}
