package com.rasanenj.warp;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

/**
 * @author gilead
 */
public class Assets {
    public static Texture shipTexture;
    public static Texture targetTexture;

    public static void load() {
        shipTexture = new Texture(Gdx.files.internal("data/grey_block.png"));
        targetTexture = new Texture(Gdx.files.internal("data/target_rectangle.png"));
    }
}
