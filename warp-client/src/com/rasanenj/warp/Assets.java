package com.rasanenj.warp;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

/**
 * @author gilead
 */
public class Assets {
    public static Texture shipTexture;
    public static Texture moveTargetTexture;
    public static Texture aimingTargetTexture;

    public static void load() {
        shipTexture = new Texture(Gdx.files.internal("data/grey_block.png"));
        moveTargetTexture = new Texture(Gdx.files.internal("data/target_rectangle.png"));
        aimingTargetTexture = new Texture(Gdx.files.internal("data/firing_target.png"));
    }
}
