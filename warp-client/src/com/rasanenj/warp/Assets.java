package com.rasanenj.warp;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * @author gilead
 */
public class Assets {
    public static Texture shipTexture, moveTargetTexture, aimingTargetTexture,
            backgroundTexture, projectileTexture;
    public static Skin skin;

    public static void load() {
        shipTexture = new Texture(Gdx.files.internal("data/grey_block.png"));
        moveTargetTexture = new Texture(Gdx.files.internal("data/target_rectangle.png"));
        aimingTargetTexture = new Texture(Gdx.files.internal("data/firing_target.png"));
        backgroundTexture = new Texture(Gdx.files.internal("data/background.png"));
        backgroundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        projectileTexture = new Texture(Gdx.files.internal("data/projectile.png"));
        skin = new Skin(Gdx.files.internal("data/uiskin.json"));
    }
}
