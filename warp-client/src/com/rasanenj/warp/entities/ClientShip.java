package com.rasanenj.warp.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

/**
 * @author gilead
 */
public class ClientShip extends Image {
    private static final Texture shipTexture = new Texture(Gdx.files.internal("data/libgdx.png"));
    private static final Texture targetTexture = new Texture(Gdx.files.internal("data/target_rectangle.png"));
    private final Image targetImg;
    Vector2 targetPos = new Vector2();

    private final float maxAngularForce = 1000;

    private final long id;

    public ClientShip(long id) {
        super(shipTexture);
        this.id = id;
        setVisible(false);
        this.targetImg = new Image(targetTexture);
        this.targetImg.setVisible(false);
        this.targetImg.setBounds(0, 0, 24, 24);
        targetPos.set(Float.NaN, Float.NaN);
    }

    public void setPosition(float x, float y) {
        if (!isVisible()) {
            setBounds(x, y, 10, 50);
            setVisible(true);
        }
        else {
            super.setPosition(x, y);
        }
    }

    public long getId() {
        return id;
    }

    public Image getTargetImg() {
        return targetImg;
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
        return maxAngularForce;
    }
}
