package com.rasanenj.warp.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import static com.rasanenj.warp.Log.log;

/**
 * @author Janne Rasanen
 */
public class TiledImage extends Image {
    private final Texture region;
    private float tileHeight, tileWidth;

    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

        int widthTimes = MathUtils.ceil(getWidth() / tileWidth);
        int heightTimes = MathUtils.ceil(getHeight() / tileHeight);

        for (int x = 0; x < widthTimes; x += 1) {
            for (float y = 0; y < heightTimes; y += 1) {
                batch.draw(region, getX() + x * tileWidth, getY() + y * tileHeight, tileWidth, tileHeight);
            }
        }
    }

    public TiledImage(Texture region) {
        super(region);
        this.region = region;
    }

    public void setTileSize(float width, float height) {
        this.tileWidth = width;
        this.tileHeight = height;
    }
}
