package com.rasanenj.warp;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.rasanenj.warp.messaging.Player;

/**
 * @author gilead
 */
public class Assets {
    public static Texture shipTexture, moveTargetTexture, aimingTargetTexture,
            backgroundTexture, projectileTexture, selectionCircleTexture, orbitCWTexture, orbitCCWTexture;
    public static Texture laserMidBackground, laserMidOverlay, laserStartBackground, laserStartOverlay, laserEndBackground, laserEndOverlay;

    public static Skin skin;

    public static void load() {
        shipTexture = new Texture(Gdx.files.internal("data/grey_block.png"));
        moveTargetTexture = new Texture(Gdx.files.internal("data/target_rectangle.png"));
        aimingTargetTexture = new Texture(Gdx.files.internal("data/firing_target.png"));
        backgroundTexture = new Texture(Gdx.files.internal("data/background.png"));
        backgroundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        projectileTexture = new Texture(Gdx.files.internal("data/projectile.png"));
        skin = new Skin(Gdx.files.internal("data/uiskin.json"));
        selectionCircleTexture = new Texture(Gdx.files.internal("data/circle.png"));
        orbitCWTexture = new Texture(Gdx.files.internal("data/orbit_cw_cursor.png"));
        orbitCCWTexture= new Texture(Gdx.files.internal("data/orbit_ccw_cursor.png"));
        laserMidBackground = new Texture(Gdx.files.internal("data/laser_mid_background.png"));
        laserMidOverlay = new Texture(Gdx.files.internal("data/laser_mid_overlay.png"));
        laserStartBackground = new Texture(Gdx.files.internal("data/laser_start_background.png"));
        laserStartOverlay = new Texture(Gdx.files.internal("data/laser_start_overlay.png"));
        laserEndBackground = new Texture(Gdx.files.internal("data/laser_end_background.png"));
        laserEndOverlay = new Texture(Gdx.files.internal("data/laser_end_overlay.png"));
    }

    public static final Color
            newCommandsColor = Color.GREEN,
            existingCommandsColor = Color.YELLOW,
            statisticsColor = new Color(30f/255f, 191f/255f, 1, 1) // dodger blue
                    ;

    private static final Color[] playerColors = {new Color(0.5f, 0, 0, 1), new Color(0, 0.5f, 0, 1), new Color(0, 0, 0.5f, 1), new Color(0, 0.5f, 0.5f, 1)};
    private static final Color[] hiliteColors = {new Color(1f, 0, 0, 1), new Color(0, 1, 0, 1), new Color(0,0,1,1), new Color(0, 1, 1, 1)};

    public static Color getHiliteColor(Player player) {
        return hiliteColors[player.getColorIndex()];
    }

    public static Color getBasicColor(Player player) {
        return playerColors[player.getColorIndex()];
    }
}
