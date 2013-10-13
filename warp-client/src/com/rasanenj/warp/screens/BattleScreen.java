package com.rasanenj.warp.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.google.gwt.i18n.client.NumberFormat;
import com.rasanenj.warp.BattleHandler;
import com.rasanenj.warp.entities.ClientShip;
import com.rasanenj.warp.messaging.ServerConnection;


import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 *
 */
public class BattleScreen implements Screen {

    private Stage stage;
    private BattleHandler battleHandler;

    private static final int CAMERA_SIZE = 20;
    private static final float zoomStep = 0.05f;
    private float zoom = 1f;
    ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final Vector2 tmp = new Vector2(), force = new Vector2();
    private final Vector2 corners[] = new Vector2[4];

    private static final Color DODGER_BLUE = new Color(30f/255f, 191f/255f, 1, 1);
    final BitmapFont font;
    private NumberFormat decimalFormatter = NumberFormat.getFormat("#.##");

    private final Array<DamageText> damageMessages = new Array(false, 16);
    private final Array<DamageText> removables = new Array<DamageText>(false, 16);

    public BattleScreen(ServerConnection conn) {
        stage = new Stage();
        stage.setViewport(CAMERA_SIZE, CAMERA_SIZE, true);

        Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));
        font = skin.getFont("default-font");
        font.setScale(0.25f);

        battleHandler = new BattleHandler(this, conn);
        for (int i=0; i < 4; i++) {
            corners[i] = new Vector2();
        }
    }

    public Stage getStage() {
        return stage;
    }

    @Override
    public void render(float delta) {
        battleHandler.update(delta);
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
        renderVectors();
        renderOffScreenShips();
        renderHealthBars();
        renderDamageTexts();
    }

    private void renderHealthBars() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (ClientShip ship : battleHandler.getShips()) {
            float health = ship.getHealth();
            float maxHealth = ship.getMaxHealth();

            float startX = ship.getX();
            float startY = ship.getY() - 0.4f;
            shapeRenderer.setColor(healthBarBG);
            shapeRenderer.rect(startX, startY, maxHealth * healthBarScale, healthBarHeight);

            shapeRenderer.setColor(healthBar);
            shapeRenderer.rect(startX, startY, health * healthBarScale, healthBarHeight);
        }
        shapeRenderer.end();
    }

    private static final Color healthBarBG = Color.RED, healthBar = Color.GREEN;
    private static final float healthBarHeight = 0.05f, healthBarScale = 0.18f;

    private void renderDamageTexts() {
        removables.clear();
        long timeNow = System.currentTimeMillis();
        for (DamageText damageText : damageMessages) {
            if (timeNow - damageText.startTime > DamageText.FADEOUT_TIME) {
                removables.add(damageText);
            }
        }

        damageMessages.removeAll(removables, true);

        if (damageMessages.size == 0) {
            return;
        }

        stage.getSpriteBatch().begin();
        for (DamageText damageText : damageMessages) {
            float fade = 1f - (timeNow - damageText.startTime) / (float) DamageText.FADEOUT_TIME;
            font.setColor(1f, 1f, 1f, fade);
            String output = decimalFormatter.format(damageText.damage);
            // String output = String.format("%f.2", damageText.damage);
            font.draw(stage.getSpriteBatch(), output, damageText.target.getX(), damageText.target.getY());
        }
        stage.getSpriteBatch().end();
    }

    private static final float TRIANGLE_SIDE = 0.5f;

    public void renderOffScreenShips() {
        // renders a triangle for every ship that is not shown on the screen
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        final Vector3 cameraPosition = stage.getCamera().position;
        final float halfViewPortWidth = stage.getCamera().viewportWidth / 2f;
        final float halfViewPortHeight = stage.getCamera().viewportHeight / 2f;

        float cameraLeft =   cameraPosition.x - halfViewPortWidth;
        float cameraRight =  cameraPosition.x + halfViewPortWidth;
        float cameraTop =    cameraPosition.y  + halfViewPortHeight;
        float cameraBottom = cameraPosition.y - halfViewPortHeight;

        boolean top = false, left = false, right = false, bottom = false;

        for (ClientShip ship : battleHandler.getShips()) {
            if (isOnScreen(ship)) {
                continue;
            }
            ship.getCenterPos(tmp);

            if (tmp.x < cameraLeft) {
                left = true;
            }
            else if (tmp.x > cameraRight) {
                right = true;
            }

            if (tmp.y > cameraTop) {
                top = true;
            }
            else if (tmp.y < cameraBottom) {
                bottom = true;
            }

            tmp.x = MathUtils.clamp(tmp.x, cameraLeft, cameraRight);
            tmp.y = MathUtils.clamp(tmp.y, cameraBottom, cameraTop);

            if (top) {
                corners[0].set(tmp.x - TRIANGLE_SIDE / 2f, tmp.y - TRIANGLE_SIDE);
                corners[1].set(tmp.x + TRIANGLE_SIDE / 2f, tmp.y - TRIANGLE_SIDE);
            }
            else if (bottom) {
                corners[0].set(tmp.x - TRIANGLE_SIDE / 2f, tmp.y + TRIANGLE_SIDE);
                corners[1].set(tmp.x + TRIANGLE_SIDE / 2f, tmp.y + TRIANGLE_SIDE);
            }
            else if (right) {
                corners[0].set(tmp.x - TRIANGLE_SIDE, tmp.y + TRIANGLE_SIDE / 2f);
                corners[1].set(tmp.x - TRIANGLE_SIDE, tmp.y - TRIANGLE_SIDE / 2f);
            }
            else if (left) {
                corners[0].set(tmp.x + TRIANGLE_SIDE, tmp.y + TRIANGLE_SIDE / 2f);
                corners[1].set(tmp.x + TRIANGLE_SIDE, tmp.y - TRIANGLE_SIDE / 2f);
            }

            shapeRenderer.setColor(ship.getColor());
            shapeRenderer.triangle(tmp.x, tmp.y, corners[0].x, corners[0].y, corners[1].x, corners[1].y);



        }
        shapeRenderer.end();
    }

    private final Vector3 tmp3 = new Vector3();

    private boolean isOnScreen(ClientShip ship) {
        ship.getBoundingBox(corners);

        final Camera camera = stage.getCamera();
        for (int i=0; i < corners.length; i++) {
            tmp3.set(corners[i].x, corners[i].y, 0);
            if (camera.frustum.pointInFrustum(tmp3)) {
                return true;
            }
        }
        return false;
    }

    public void renderVectors() {
        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (ClientShip ship : battleHandler.getShips()) {
            ship.getCenterPos(tmp);
            /*
            // render the velocity of the ship
            shapeRenderer.setColor(DODGER_BLUE);
            shapeRenderer.line(tmp.x, tmp.y,
                    tmp.x + ship.getVelocity().x, tmp.y + ship.getVelocity().y);
                    */


            // render the rectangle that limits the maximum force vector
            /*
            ship.getForceLimitCorners(corners);
            shapeRenderer.setColor(Color.YELLOW);
            for (int i=0; i < 4; i++) {
                // first we make a vector from i'th corner to i+1'th corner
                int next = i+1;
                if (next == 4) {
                    next = 0; // last vector is from first point to the last point
                }
                shapeRenderer.line(corners[i].x, corners[i].y,
                             corners[next].x, corners[next].y);
            }
            */

            // render the ideal impulse
            /*
            shapeRenderer.setColor(Color.BLUE);
            shapeRenderer.line(tmp.x, tmp.y,
                    tmp.x + ship.getImpulseIdeal().x, tmp.y + ship.getImpulseIdeal().y);
            */

            // render the impulse
            shapeRenderer.setColor(DODGER_BLUE);
            shapeRenderer.line(tmp.x, tmp.y,
                    tmp.x + ship.getImpulse().x, tmp.y + ship.getImpulse().y);
        }
        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        //stage.setViewport(width, height, true);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void pause() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void resume() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    public void setCameraPos(float x, float y) {
        float dx = x - stage.getCamera().position.x;
        float dy = y - stage.getCamera().position.y;
        stage.getCamera().translate(dx, dy, 0);
    }

    public void translateCamera(float dx, float dy) {
        stage.getCamera().translate(dx, dy, 0);
    }

    public void zoom(int amount) {
        zoom += zoomStep * amount;
        log("zoom:" + zoom);
        stage.setViewport(CAMERA_SIZE * zoom, CAMERA_SIZE * zoom, true);
    }

    public void addDamageText(ClientShip target, float damage) {
        damageMessages.add(new DamageText(target, damage));
    }

    private class DamageText {
        public static final long FADEOUT_TIME = 1000; // in ms
        final ClientShip target;
        final float damage;
        final long startTime;

        public DamageText(ClientShip target, float damage) {
            startTime = System.currentTimeMillis();
            this.target = target;
            this.damage = damage;
        }
    }
}
