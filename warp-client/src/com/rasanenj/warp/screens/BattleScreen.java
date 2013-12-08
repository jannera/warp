package com.rasanenj.warp.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.google.gwt.i18n.client.NumberFormat;
import com.rasanenj.warp.BattleHandler;
import com.rasanenj.warp.Settings;
import com.rasanenj.warp.entities.ClientShip;
import com.rasanenj.warp.messaging.ServerConnection;


import static com.rasanenj.warp.Log.log;

/**
 * Handles rendering and Screen events.
 *
 * @author gilead
 */
public class BattleScreen implements Screen {

    private final Matrix4 normalProjection;
    private Stage stage;
    private BattleHandler battleHandler;

    private static final int CAMERA_SIZE = 20;
    ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final Vector2 tmp = new Vector2(), force = new Vector2();
    private final Vector2 corners[] = new Vector2[4];
    private static final int CIRCLE_SEGMENTS = 64;

    private static final Color DODGER_BLUE = new Color(30f/255f, 191f/255f, 1, 1);
    final BitmapFont font;
    private NumberFormat decimalFormatter = NumberFormat.getFormat("#.##");

    private final Array<DamageText> damageMessages = new Array(false, 16);
    private final Array<DamageText> removables = new Array<DamageText>(false, 16);
    private final OrthographicCamera cam;
    private boolean selectionRectangleActive = false;
    private final Vector2 selectionRectangleStart = new Vector2(), getSelectionRectangleEnd = new Vector2();

    // only for drawing directly on the screen, like rendering text in screen coordinates
    private final SpriteBatch batch = new SpriteBatch();

    private OptimalRenderingState optimalRenderingState = OptimalRenderingState.ALL_SHIPS;
    private Vector2 manualSteeringEnd;
    private Vector2 manualSteeringStart;
    private boolean manualSteeringActive = false;

    public OrthographicCamera getCam() {
        return cam;
    }

    public enum OptimalRenderingState {
        SELECTED_SHIPS, OWN_SHIPS, ENEMY_SHIPS, ALL_SHIPS, NOTHING
    }

    public void cycleOptimalRendering() {
        if (optimalRenderingState == OptimalRenderingState.SELECTED_SHIPS) {
            optimalRenderingState = OptimalRenderingState.OWN_SHIPS;
        }
        else if (optimalRenderingState == OptimalRenderingState.OWN_SHIPS) {
            optimalRenderingState = OptimalRenderingState.ENEMY_SHIPS;
        }
        else if (optimalRenderingState == OptimalRenderingState.ENEMY_SHIPS) {
            optimalRenderingState = OptimalRenderingState.ALL_SHIPS;
        }
        else if (optimalRenderingState == OptimalRenderingState.ALL_SHIPS) {
            optimalRenderingState = OptimalRenderingState.NOTHING;
        }
        else if (optimalRenderingState == OptimalRenderingState.NOTHING) {
            optimalRenderingState = OptimalRenderingState.SELECTED_SHIPS;
        }
    }

    public BattleScreen(ServerConnection conn) {
        stage = new Stage();
        stage.setViewport(CAMERA_SIZE, CAMERA_SIZE, true);
        cam = (OrthographicCamera) stage.getCamera();

        Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));
        font = skin.getFont("default-font");

        battleHandler = new BattleHandler(this, conn);
        for (int i=0; i < 4; i++) {
            corners[i] = new Vector2();
        }
        normalProjection = new Matrix4();
        normalProjection.setToOrtho2D(0, 0, Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight());
        batch.setProjectionMatrix(normalProjection);
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
        renderOptimals();
        renderDamageTexts();
        renderShipTexts();
        renderManualSteering();
        renderDebugText();
        renderSelectionRectangle();
        renderPhysicsVertices();
    }

    private void renderManualSteering() {
        if (!manualSteeringActive) {
            return;
        }

        shapeRenderer.setProjectionMatrix(normalProjection);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        log(manualSteeringStart + " - " + manualSteeringEnd);
        shapeRenderer.line(manualSteeringStart, manualSteeringEnd);
        shapeRenderer.end();
        shapeRenderer.setProjectionMatrix(cam.combined);
        // TODO: make this a real arrow (i.e. draw those wedges on the end of the line)
    }

    private static final Color SHIP_TEXT_COLOR = Color.WHITE;

    private void renderShipTexts() {
        batch.begin();
        for (ClientShip s : battleHandler.getShips()) {
            font.setColor(SHIP_TEXT_COLOR);
            String output = s.getText();
            tmp3.set(s.getX(), s.getY() - s.getHeight() * 1.5f, 0);
            cam.project(tmp3);
            font.drawMultiLine(batch, output, tmp3.x, tmp3.y);
        }
        batch.end();
    }

    private void renderPhysicsVertices() {
        if (!Settings.renderPhysicsFixtures) {
            return;
        }
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1,1,1,1);
        for (ClientShip s : battleHandler.getShips()) {
            Vector2[] verts = s.getVertices();
            for (int i=0; i < verts.length; i++) {
                // first we make a vector from i'th corner to i+1'th corner
                int next = i+1;
                if (next == verts.length) {
                    next = 0; // last vector is from first point to the last point
                }
                shapeRenderer.line(verts[i].x,    verts[i].y,
                                   verts[next].x, verts[next].y);
            }
        }
        shapeRenderer.end();
    }

    private void renderSelectionRectangle() {
        if (!selectionRectangleActive) {
            return;
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(selectionRectangleStart.x, selectionRectangleStart.y,
                getSelectionRectangleEnd.x, getSelectionRectangleEnd.y);
        shapeRenderer.end();
    }

    private void renderOptimals() {
        if (optimalRenderingState == OptimalRenderingState.NOTHING) {
            return;
        }
        if (optimalRenderingState == OptimalRenderingState.SELECTED_SHIPS) {
            for (ClientShip s : battleHandler.getSelectedShips()) {
                renderOptimals(s);
            }
            return;
        }

        long myid = battleHandler.getMyId();

        for (ClientShip ship : battleHandler.getShips()) {
            if (optimalRenderingState == OptimalRenderingState.ALL_SHIPS ||
                optimalRenderingState == OptimalRenderingState.OWN_SHIPS && ship.getOwner().getId() == myid ||
                optimalRenderingState == OptimalRenderingState.ENEMY_SHIPS && ship.getOwner().getId() != myid) {
                renderOptimals(ship);
            }
        }
    }

    private void renderOptimals(ClientShip ship) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        ship.getCenterPos(tmp);
        float optimal = ship.getStats().getWeaponOptimal();
        float falloff = ship.getStats().getWeaponFalloff();
        shapeRenderer.setColor(Color.ORANGE);
        shapeRenderer.circle(tmp.x, tmp.y, optimal, CIRCLE_SEGMENTS);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.circle(tmp.x, tmp.y, optimal + falloff, CIRCLE_SEGMENTS);
        shapeRenderer.end();
    }

    private void renderHealthBars() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (ClientShip ship : battleHandler.getShips()) {
            float health = ship.getHealth();
            float maxHealth = ship.getStats().getMaxHealth();

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

        batch.begin();
        for (DamageText damageText : damageMessages) {
            float fade = 1f - (timeNow - damageText.startTime) / (float) DamageText.FADEOUT_TIME;
            font.setColor(fade, 0, 0, fade);
            String output = decimalFormatter.format(damageText.damage);
            // String output = String.format("%f.2", damageText.damage);
            tmp3.set(damageText.target.getX(), damageText.target.getY() - damageText.target.getHeight() * 1.5f, 0);
            cam.project(tmp3);
            font.draw(batch, output, tmp3.x, tmp3.y);
        }
        batch.end();
    }

    private static final float TRIANGLE_SIDE = 0.5f;

    public void renderOffScreenShips() {
        // renders a triangle for every ship that is not shown on the screen
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        final Vector3 cameraPosition = cam.position;
        final float halfViewPortWidth = cam.viewportWidth / 2f * cam.zoom;
        final float halfViewPortHeight = cam.viewportHeight / 2f * cam.zoom;

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

            shapeRenderer.setColor(ship.getImage().getColor());
            shapeRenderer.triangle(tmp.x, tmp.y, corners[0].x, corners[0].y, corners[1].x, corners[1].y);



        }
        shapeRenderer.end();
    }

    private final Vector3 tmp3 = new Vector3();

    private boolean isOnScreen(ClientShip ship) {
        ship.getBoundingBox(corners);

        for (int i=0; i < corners.length; i++) {
            tmp3.set(corners[i].x, corners[i].y, 0);
            if (cam.frustum.pointInFrustum(tmp3)) {
                return true;
            }
        }
        return false;
    }

    public void renderVectors() {
        shapeRenderer.setProjectionMatrix(cam.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (ClientShip ship : battleHandler.getShips()) {
            ship.getCenterPos(tmp);

            if (Settings.renderShipVelocity) {
                shapeRenderer.setColor(DODGER_BLUE);
                shapeRenderer.line(tmp.x, tmp.y,
                        tmp.x + ship.getVelocity().x, tmp.y + ship.getVelocity().y);
            }


            if (Settings.renderMaxForceRectangle) {
            // render the rectangle that limits the maximum force vector
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
            }

            if (Settings.renderIdealImpulse) {
                shapeRenderer.setColor(Color.BLUE);
                shapeRenderer.line(tmp.x, tmp.y,
                        tmp.x + ship.getImpulseIdeal().x, tmp.y + ship.getImpulseIdeal().y);
            }

            if (Settings.renderImpulse) {
                shapeRenderer.setColor(DODGER_BLUE);
                shapeRenderer.line(tmp.x, tmp.y,
                        tmp.x + ship.getImpulse().x, tmp.y + ship.getImpulse().y);

            }
        }
        shapeRenderer.end();
    }

    private void renderDebugText() {
        batch.begin();
        font.setColor(1, 1, 1, 1);
        String debugText = Gdx.graphics.getFramesPerSecond() + " fps, " + battleHandler.getShips().size() + " ships";
        font.draw(batch, debugText, 2, 20);
        batch.end();
        if (!Settings.renderAcceleration) {
            return;
        }

        stage.getSpriteBatch().begin();
        for (ClientShip ship : battleHandler.getShips()) {
            font.setColor(1f, 1f, 1f, 1);
            String output = decimalFormatter.format(ship.getAcceleration());
            // String output = String.format("%f.2", damageText.damage);
            font.draw(stage.getSpriteBatch(), output, ship.getX(), ship.getY());
        }
        stage.getSpriteBatch().end();
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
        float dx = x - cam.position.x;
        float dy = y - cam.position.y;
        cam.translate(dx, dy, 0);
        cam.update();
    }

    public void translateCamera(float dx, float dy) {
        cam.translate(dx, dy, 0);
        cam.update();
    }

    public void zoom(float amount, float x, float y) {
        if (cam.zoom + amount <= 0) {
            return;
        }

        cam.zoom += amount;
        // TODO: the following deltas are not optimal, but I suppose they're close enough for proto
        final float deltaX = (cam.position.x - x /* - CAMERA_SIZE/2f */) * amount;
        final float deltaY = (cam.position.y - y /* - CAMERA_SIZE/2f*/ ) * amount;

        cam.translate(deltaX, deltaY, 0f);
        cam.update();
    }

    public void addDamageText(ClientShip target, float damage) {
        damageMessages.add(new DamageText(target, damage));
    }
    /*
    public void createNPC(String host) {
        battleHandler.createNPC(host);
    }
    */

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

    public void setSelectionRectangleStart(float x, float y) {
        selectionRectangleStart.set(x, y);
    }

    public void setSelectionRectangleEnd(float x, float y) {
        getSelectionRectangleEnd.set(x, y);
    }

    public void setSelectionRectangleActive(boolean active) {
        this.selectionRectangleActive = active;
    }

    public void setManualSteeringLine(Vector2 start, Vector2 end) {
        this.manualSteeringStart = start;
        this.manualSteeringEnd = end;
    }

    public void setManualSteering(boolean active) {
        this.manualSteeringActive = active;
    }
}
