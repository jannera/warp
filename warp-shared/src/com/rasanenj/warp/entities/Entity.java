package com.rasanenj.warp.entities;

/**
 * @author gilead
 */
public class Entity {
    private final long id;

    public Entity() {
        this.id = generateId();
    }

    public Entity(long id) {
        this.id = id;
    }

    private static long idCount = 0;
    private static long generateId() {
        return idCount++;
    }

    public long getId() {
        return id;
    }
}
