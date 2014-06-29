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
    private static Texture shipTextureBattleship;
    private static Texture[] shipTextureFrigateBase, shipTextureFrigateHilite,
            shipTextureCruiserHilite, shipTextureCruiserBase;
    public static Texture moveTargetTexture, backgroundTexture, projectileTexture, selectionCircleTexture, orbitCWTexture, orbitCCWTexture;
    public static Texture laserMidBackground, laserMidOverlay, laserStartBackground, laserStartOverlay, laserEndBackground, laserEndOverlay;

    public static Skin skin;

    public static void load() {
        moveTargetTexture = new Texture(Gdx.files.internal("data/target_rectangle.png"));
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

        // TODO: handle these dynamically by reading ship types and image file names from the ship catalog
        Pixmap frigBase = createShipPixmap("data/frigate_base.png");
        Pixmap frigOverlay = createShipPixmap("data/frigate_overlay.png");
        shipTextureFrigateBase = createShipTextures(frigBase, frigOverlay, playerColors);
        shipTextureFrigateHilite = createShipTextures(frigBase, frigOverlay, hiliteColors);

        Pixmap cruiserBase = createShipPixmap("data/cruiser_base.png");
        Pixmap cruiserOverlay = createShipPixmap("data/cruiser_overlay.png");
        shipTextureCruiserBase = createShipTextures(cruiserBase, cruiserOverlay, playerColors);
        shipTextureCruiserHilite = createShipTextures(cruiserBase, cruiserOverlay, hiliteColors);

        shipTextureBattleship = new Texture(Gdx.files.internal("data/ship_battleship.png"));
    }

    private static Texture[] createShipTextures(Pixmap base, Pixmap overlay, Color[] colors) {
        Texture[] result = new Texture[colors.length];

        int i = 0;
        for(Color c : colors) {
            tintPixmap(overlay, new Color(c.r, c.g, c.b, 1f));

            Pixmap amalgam = new Pixmap(base.getWidth(), base.getHeight(), base.getFormat());

            amalgam.drawPixmap(base, 0, 0);
            amalgam.drawPixmap(overlay, 0,0);
            result[i++] = new Texture(amalgam);
        }
        return result;
    }

    private static Pixmap createShipPixmap(String filename) {
        return rotate90DegreesP(new Pixmap(Gdx.files.internal(filename)));
    }

    private static Pixmap rotate90DegreesP(Pixmap p) {
        Pixmap result = new Pixmap(p.getHeight(), p.getWidth(), p.getFormat());
        for (int x = 0; x < p.getWidth(); x++) {
            for (int y = 0; y < p.getHeight(); y++) {
                result.drawPixel(y, x, p.getPixel(x, p.getHeight() - y));
            }
        }
        return result;
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
                return shipTextureCruiserBase[owner.getColorIndex()];
            case BATTLESHIP:
                return shipTextureBattleship;
            default:
                return null;
        }
    }

    public static Texture getShipBaseTexture(ShipStats.Shiptype type) {
        switch (type) {
            case FRIGATE:
                return shipTextureFrigateBase[0];
            case CRUISER:
                return shipTextureCruiserBase[0];
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
                return shipTextureCruiserHilite[owner.getColorIndex()];
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
