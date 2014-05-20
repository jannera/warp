package com.rasanenj.warp;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.rasanenj.warp.entities.ShipStats;
import com.rasanenj.warp.messaging.Player;

/**
 * @author gilead
 */
public class Assets {
    public static Texture shipTextureCruiser, shipTextureBattleship;
    public static Texture[] shipTextureFrigateBase, shipTextureFrigateHilite;
    public static Texture moveTargetTexture, aimingTargetTexture,
            backgroundTexture, projectileTexture, selectionCircleTexture, orbitCWTexture, orbitCCWTexture,
            targetValueMarker;
    public static Texture laserMidBackground, laserMidOverlay, laserStartBackground, laserStartOverlay, laserEndBackground, laserEndOverlay;

    public static Skin skin;

    public static void load() {
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

        shipTextureFrigateBase = createShipTextures("data/frigate_base.png", "data/frigate_overlay.png", playerColors);
        shipTextureFrigateHilite = createShipTextures("data/frigate_base.png", "data/frigate_overlay.png", hiliteColors);
        shipTextureCruiser = new Texture(Gdx.files.internal("data/ship_cruiser.png"));
        shipTextureBattleship = new Texture(Gdx.files.internal("data/ship_battleship.png"));
        targetValueMarker = new Texture(Gdx.files.internal("data/grey_block.png"));
    }

    private static Texture[] createShipTextures(String baseFile, String overlayFile, Color[] colors) {
        Texture[] result = new Texture[colors.length];

        int i = 0;
        for(Color c : colors) {
            result[i++] = createShipTexture(baseFile, overlayFile, new Color(c.r, c.g, c.b, 0.5f));
        }
        return result;
    }

    private static Texture createShipTexture(String baseFile, String overlayFile, Color c) {
        Pixmap base = new Pixmap(Gdx.files.internal(baseFile));
        Pixmap overlay = new Pixmap(Gdx.files.internal(overlayFile));

        tintPixmap(overlay, c);

        Pixmap result = new Pixmap(base.getWidth(), base.getHeight(), base.getFormat());

        result.drawPixmap(base, 0, 0);
        result.drawPixmap(overlay, 0,0);
        return rotate90Degrees(result);
    }

    private static Texture rotate90Degrees(Pixmap p) {
        FrameBuffer fbo = new FrameBuffer(p.getFormat(), p.getWidth(), p.getHeight(), false);
        SpriteBatch batch = new SpriteBatch();
        Texture t = new Texture(p);

        fbo.begin();

        Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(new TextureRegion(t), 0, 0, t.getWidth()/2f, t.getHeight()/2f, t.getHeight(), t.getWidth(), 1f, 1f, 0, true);
        batch.end();
        fbo.end();

        return fbo.getColorBufferTexture();
    }

    /*
     * Tints the given Pixmap with the given Color only on pixels that already have content in them
     */
    private static void tintPixmap(Pixmap p, Color c) {
        p.setColor(c);
        for (int x=0; x < p.getWidth(); x++) {
            for (int y=0; y < p.getHeight(); y++) {
                if (p.getPixel(x, y) > 0) {
                    p.drawPixel(x, y);
                }
            }
        }
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

    public static Texture getShipBaseTexture(ShipStats.Shiptype type, Player owner) {
        switch (type) {
            case FRIGATE:
                return shipTextureFrigateBase[owner.getColorIndex()];
            case CRUISER:
                return shipTextureCruiser;
            case BATTLESHIP:
                return shipTextureBattleship;
            default:
                return null;
        }
    }

    public static Texture getShipHiliteTexture(ShipStats.Shiptype type, Player owner) {
        switch (type) {
            case FRIGATE:
                return shipTextureFrigateHilite[owner.getColorIndex()];
            case CRUISER:
                return shipTextureCruiser;
            case BATTLESHIP:
                return shipTextureBattleship;
            default:
                return null;
        }
    }

    public static Color getLaserColor(Player shooter) {
        return hiliteColors[shooter.getColorIndex()];
    }
}
