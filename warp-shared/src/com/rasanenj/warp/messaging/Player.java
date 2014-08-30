package com.rasanenj.warp.messaging;

import com.rasanenj.warp.entities.Entity;

/**
 * @author gilead
 */
public class Player extends Entity {
    protected final String name;
    protected final int colorIndex;
    protected float score;

    public Player(String name, int colorIndex) {
        this.name = name;
        this.colorIndex = colorIndex;
    }

    public Player(String name, long id, int colorIndex) {
        super(id);
        this.name = name;
        this.colorIndex = colorIndex;
    }

    public String getName() {
        return name;
    }

    public int getColorIndex() {
        return colorIndex;
    }

    public String toString() {
        return name + " (" + getId() + ", " + colorIndex + ")";
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }
}
