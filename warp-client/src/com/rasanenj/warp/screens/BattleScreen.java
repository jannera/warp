package com.rasanenj.warp.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Random;
import com.rasanenj.warp.*;
import com.rasanenj.warp.actors.TiledImage;
import com.rasanenj.warp.entities.ClientShip;
import com.rasanenj.warp.messaging.ServerConnection;
import com.rasanenj.warp.systems.ShipShooting;
import com.rasanenj.warp.tasks.PathPlotterTask;


import static com.rasanenj.warp.Log.log;

/**
 * Handles rendering and Screen events.
 *
 * @author gilead
 */
public class BattleScreen implements Screen {

    private final Matrix4 normalProjection;
    private final TiledImage backgroundImage;
    private Stage stage;
    private BattleHandler battleHandler;

    private static final int CAMERA_SIZE = 20;
    private final static float GRID_SIZE = 8;
    private final static float NAVIGATION_TARGET_SIZE = 60f;
    private final static float ACTIVE_FIRING_TARGET_SIZE = 60f;
    private final static float ACTIVE_ORBIT_TARGET_SIZE = 60f;
    private final static float HOVERING_FIRING_TARGET_SIZE = 90f;
    ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final Vector2 tmp = new Vector2(), force = new Vector2();
    private final Vector2 corners[] = new Vector2[4];
    private static final int CIRCLE_SEGMENTS = 64;

    private static final float PROJECTILE_SIZE = 3f;

    final BitmapFont font;
    private NumberFormat decimalFormatter = NumberFormat.getFormat("#.##");

    private final Array<DamageText> damageMessages = new Array(false, 16);
    private final Array<DamageText> removables = new Array<DamageText>(false, 16);

    private final Array<DamageProjectile> damageProjectiles = new Array(false, 16);
    private final Array<DamageProjectile> removableProjectiles = new Array(false, 16);
    private final OrthographicCamera cam;
    private boolean selectionRectangleActive = false;
    private final Vector3 selectionRectangleStart = new Vector3(), getSelectionRectangleEnd = new Vector3();

    // only for drawing directly on the screen, like rendering text in screen coordinates
    private final SpriteBatch batch = new SpriteBatch();

    private OptimalRenderingState optimalRenderingState = OptimalRenderingState.ALL_SHIPS;
    private Vector2 manualSteeringEnd;
    private Vector2 manualSteeringStart;
    private boolean manualSteeringActive = false;

    private ClientShip hoveringTarget = null;

    private final Array<PathPlotterTask> plotters = new Array<PathPlotterTask>(false, 0);
    private OrbitUIHandler orbitUIHandler;

    public OrthographicCamera getCam() {
        return cam;
    }

    public void addPlotter(PathPlotterTask p) {
        plotters.add(p);
    }

    public boolean isPlotting(ClientShip s) {
        for (PathPlotterTask p : plotters) {
            if (p.getShip() == s) {
                return true;
            }
        }
        return false;
    }

    public void setOrbitUIHandler(OrbitUIHandler orbitUIHandler) {
        this.orbitUIHandler = orbitUIHandler;
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

    public BattleScreen(ServerConnection conn, LobbyScreen lobbyScreen) {
        stage = new Stage();
        stage.setViewport(CAMERA_SIZE, CAMERA_SIZE, true);
        cam = (OrthographicCamera) stage.getCamera();

        font = Assets.skin.getFont("default-font");

        battleHandler = new BattleHandler(this, conn, lobbyScreen);
        for (int i=0; i < 4; i++) {
            corners[i] = new Vector2();
        }
        normalProjection = new Matrix4();
        normalProjection.setToOrtho2D(0, 0, Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight());
        batch.setProjectionMatrix(normalProjection);
        this.backgroundImage = new TiledImage(Assets.backgroundTexture);
        stage.addActor(backgroundImage);
        backgroundImage.setTileSize(GRID_SIZE, GRID_SIZE);
        backgroundImage.setZIndex(0);
        cam.zoom = 2.5f;
    }

    public Stage getStage() {
        return stage;
    }

    @Override
    public void render(float delta) {
        battleHandler.update(delta);
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        updateBackground();
        updateMessages();
        updateProjectiles();
        stage.act(Gdx.graphics.getDeltaTime());
        estimateShipPositions();
        stage.draw();

        renderPositionProjections();

        renderPaths();
        renderVectors();
        renderOffScreenShips();
        renderProjectiles();
        renderSelectionCircles();
        renderHealthBars();

        // render active commands for selected ships
        renderOrbitTargets();
        renderNavigationTargets();
        renderFiringTargets();

        renderHoveringTarget();
        renderOptimals();
        renderOrbitCircle();
        renderDamageTexts();
        renderShipTexts();
        renderManualSteering();
        renderDebugText();
        renderSelectionRectangle();
        renderPhysicsVertices();

        renderTextBelowMouseCursor();
    }

    private void renderPositionProjections() {
        if (!Settings.renderPositionProjections) {
            return;
        }

        shapeRenderer.setProjectionMatrix(normalProjection);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        Color c = new Color();

        for (ClientShip ship : battleHandler.getShips()) {
            boolean first = true;
            c.set(ship.getImage().getColor());
            for (PositionProjection projection : ship.getProjectedPositions()) {
                if (first) {
                    first = false;
                }
                float scale = 1f - 0.75f * (float) projection.getTimestamp() / (float) ShipShooting.PROJECTION_TIME_MS;
                shapeRenderer.setColor(scale * c.r, scale * c.g, scale * c.b, 1);
                tmp3.set(projection.getPosition().x, projection.getPosition().y, 0);
                cam.project(tmp3);
                shapeRenderer.circle(tmp3.x, tmp3.y, 3);
            }
        }
        shapeRenderer.end();
        shapeRenderer.setProjectionMatrix(cam.combined);
    }

    private void estimateShipPositions() {
        final long timeNow = System.currentTimeMillis();
        for (ClientShip ship : battleHandler.getShips()) {
            ship.updatePos(timeNow);
        }
    }

    final Array<Float> velocities = new Array<Float>(false, 16);

    private void renderTextBelowMouseCursor() {
        if (orbitUIHandler.getState() != OrbitUIHandler.State.SELECTING_RADIUS) {
            return;
        }

        float orbitCircleRadius = orbitUIHandler.getOrbitRadius();


        velocities.clear();
        for (ClientShip s : battleHandler.getSelectedShips()) {
            float vel = s.getDesiredVelocity();
            if (!velocities.contains(vel, false)) {
                velocities.add(vel);
            }
        }

        String textBelowCursor = "";
        for (Float f : velocities) {
            float angularSpeed = Geometry.getAngularSpeed(f, orbitCircleRadius);
            if (!textBelowCursor.isEmpty()) {
                textBelowCursor += ", ";
            }
            textBelowCursor += decimalFormatter.format(angularSpeed);
        }

        float x = Gdx.input.getX();
        float y = Gdx.graphics.getHeight() - Gdx.input.getY() - 30f;

        batch.begin();
        font.setColor(1, 1, 1, 1);
        font.draw(batch, textBelowCursor, x, y);
        batch.end();
    }

    private void renderOrbitTargets() {
        batch.begin();
        final float halfSize = ACTIVE_ORBIT_TARGET_SIZE / 2f;
        for (ClientShip ship : battleHandler.getSelectedShips()) {
            Texture t = Assets.orbitCCWTexture;
            if (ship.isOrbitCW()) {
                t = Assets.orbitCWTexture;
            }
            renderAtTarget(ship.getOrbitShip(), t, ACTIVE_ORBIT_TARGET_SIZE, halfSize);
        }
        batch.end();
    }

    private void renderAtTarget(ClientShip target, Texture tex, float size, float halfSize) {
        if (target == null) {
            return;
        }
        target.getCenterPos(tmp);
        tmp3.set(tmp.x, tmp.y, 0);
        cam.project(tmp3);
        batch.draw(tex, tmp3.x - halfSize, tmp3.y - halfSize, size, size);
    }

    private void renderFiringTargets() {
        batch.begin();
        final float halfSize = ACTIVE_FIRING_TARGET_SIZE / 2f;
        for (ClientShip ship : battleHandler.getSelectedShips()) {
            renderAtTarget(ship.getFiringTarget(), Assets.aimingTargetTexture,
                    ACTIVE_FIRING_TARGET_SIZE, halfSize);
        }
        batch.end();
    }

    private void renderOrbitCircle() {
        // log("orbit " + orbitCircleTarget + " @ " + orbitCircleRadius);
        if (orbitUIHandler.getState() != OrbitUIHandler.State.SELECTING_RADIUS) {
            return;
        }

        ClientShip orbitCircleTarget = orbitUIHandler.getOrbitTargetShip();

        if (orbitCircleTarget == null) {
            return;
        }

        float orbitCircleRadius = orbitUIHandler.getOrbitRadius();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Assets.newCommandsColor);
        orbitCircleTarget.getCenterPos(tmp);
        shapeRenderer.circle(tmp.x, tmp.y, orbitCircleRadius);
        shapeRenderer.end();
    }

    private void renderSelectionCircles() {
        batch.begin();

        for (ClientShip ship : battleHandler.getShips()) {
            if (!ship.isCircled()) {
                continue;
            }
            ship.getCenterPos(tmp);
            tmp3.set(tmp.x, tmp.y, 0);
            cam.project(tmp3);
            batch.setColor(ship.getCircleColor());
            float width = 60, height = width;
            batch.draw(Assets.selectionCircleTexture, tmp3.x - width/2f, tmp3.y - height/2f, width, height);
        }
        batch.end();
    }

    private void renderPaths() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLUE);
        for (PathPlotterTask p : plotters) {
            Vector2 earlier = null;
            for (Vector2 current : p) {
                if (Float.isNaN(current.x)) {
                    continue;
                }
                if (earlier != null) {
                    // not first in the list
                    shapeRenderer.line(earlier, current);
                }
                earlier = current;
            }
        }
        shapeRenderer.end();
    }

    private void renderProjectiles() {
        if (damageProjectiles.size == 0) {
            return;
        }
        long timeNow = System.currentTimeMillis();
        batch.begin();
        batch.setColor(Color.GRAY);
        for (DamageProjectile projectile : damageProjectiles) {
            float fade = (timeNow - projectile.startTime) / projectile.timeToLive;
            tmp3.set(projectile.startX, projectile.startY, 0);
            tmp3end.set(projectile.target.getX(), projectile.target.getY() - projectile.target.getHeight() * 1.5f, 0);
            tmp3.lerp(tmp3end, fade);
            cam.project(tmp3);
            batch.draw(Assets.projectileTexture, tmp3.x, tmp3.y, PROJECTILE_SIZE, PROJECTILE_SIZE);
        }
        batch.end();
    }

    private void updateMessages() {
        removables.clear();
        long timeNow = System.currentTimeMillis();
        for (DamageText damageText : damageMessages) {
            if (timeNow - damageText.startTime > DamageText.FADEOUT_TIME) {
                removables.add(damageText);
            }
        }

        damageMessages.removeAll(removables, true);
    }

    private void updateProjectiles() {
        removableProjectiles.clear();
        long timeNow = System.currentTimeMillis();
        for (DamageProjectile projectile : damageProjectiles) {
            if (timeNow - projectile.startTime > projectile.timeToLive) {
                removableProjectiles.add(projectile);
                addDamageText(projectile.target, projectile.damage);
            }
        }

        damageProjectiles.removeAll(removableProjectiles, true);
    }

    private void renderHoveringTarget() {
        if (hoveringTarget == null) {
            return;
        }
        batch.begin();
        final float width = HOVERING_FIRING_TARGET_SIZE;
        final float height = width;
        tmp3.set(hoveringTarget.getX(), hoveringTarget.getY(), 0);
        cam.project(tmp3);
        tmp3.sub(width / 2f, height / 2f, 0);
        batch.draw(Assets.aimingTargetTexture, tmp3.x, tmp3.y, width, height);
        batch.end();
    }

    private void renderNavigationTargets() {
        batch.begin();
        float halfWidth = NAVIGATION_TARGET_SIZE / 2f;
        float halfHeight = NAVIGATION_TARGET_SIZE / 2f;
        for (ClientShip ship : battleHandler.getSelectedShips()) {
            if (!ship.hasTargetPos()) {
                continue;
            }
            tmp3.set(ship.getTargetPos().x, ship.getTargetPos().y, 0);
            cam.project(tmp3);
            batch.draw(Assets.moveTargetTexture, tmp3.x - halfWidth, tmp3.y - halfHeight, NAVIGATION_TARGET_SIZE, NAVIGATION_TARGET_SIZE);
        }
        batch.end();
    }

    // moves the background tiled image to start near where the camera is now
    private void updateBackground() {
        final Vector3 cameraPosition = cam.position;
        final float halfViewPortWidth = cam.viewportWidth / 2f * cam.zoom;
        final float halfViewPortHeight = cam.viewportHeight / 2f * cam.zoom;

        // coordinates of left bottom of the screen in world coordinates
        float cameraX =   cameraPosition.x - halfViewPortWidth;
        float cameraY = cameraPosition.y - halfViewPortHeight;

        // find the next smallest grid starting point
        float startX = MathUtils.floor(cameraX / GRID_SIZE) * GRID_SIZE;
        float startY = MathUtils.floor(cameraY / GRID_SIZE) * GRID_SIZE;

        backgroundImage.setBounds(startX, startY, halfViewPortWidth * 2f + GRID_SIZE, halfViewPortHeight * 2f + GRID_SIZE);
    }

    private void renderManualSteering() {
        if (!manualSteeringActive) {
            return;
        }

        shapeRenderer.setProjectionMatrix(normalProjection);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        // log(manualSteeringStart + " - " + manualSteeringEnd);
        shapeRenderer.line(manualSteeringStart, manualSteeringEnd);
        shapeRenderer.end();
        shapeRenderer.setProjectionMatrix(cam.combined);
        // TODO: render these for all owned ships
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

        shapeRenderer.setProjectionMatrix(normalProjection);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(selectionRectangleStart.x, selectionRectangleStart.y,
                getSelectionRectangleEnd.x - selectionRectangleStart.x, getSelectionRectangleEnd.y - selectionRectangleStart.y);
        shapeRenderer.end();
        shapeRenderer.setProjectionMatrix(cam.combined);
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
        shapeRenderer.setProjectionMatrix(normalProjection);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (ClientShip ship : battleHandler.getShips()) {
            float health = ship.getHealth();
            float maxHealth = ship.getStats().getMaxHealth();


            tmp3.set(ship.getX(), ship.getY(), 0);
            cam.project(tmp3);
            shapeRenderer.setColor(healthBarBG);
            tmp3.y -= 10f;
            shapeRenderer.rect(tmp3.x, tmp3.y, maxHealth * healthBarScale / cam.zoom, healthBarHeight);

            shapeRenderer.setColor(healthBar);
            shapeRenderer.rect(tmp3.x, tmp3.y, health * healthBarScale / cam.zoom, healthBarHeight);
        }
        shapeRenderer.end();
        shapeRenderer.setProjectionMatrix(cam.combined);
    }

    private static final Color healthBarBG = Color.RED, healthBar = Color.GREEN;
    private static final float healthBarHeight = 2f, healthBarScale = 1f;

    private void renderDamageTexts() {
        if (damageMessages.size == 0) {
            return;
        }
        long timeNow = System.currentTimeMillis();
        batch.begin();
        for (DamageText damageText : damageMessages) {
            float fade = 1f - (timeNow - damageText.startTime) / (float) DamageText.FADEOUT_TIME;
            font.setColor(fade, 0, 0, fade);
            String output = decimalFormatter.format(damageText.damage);
            // String output = String.format("%f.2", damageText.damage);
            tmp3.set(damageText.target.getX() + damageText.offsetX, damageText.target.getY()
                    - damageText.target.getHeight() * 1.5f + damageText.offsetY, 0);
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

    private final Vector3 tmp3 = new Vector3(), tmp3end = new Vector3();

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
                shapeRenderer.setColor(Assets.statisticsColor);
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
                shapeRenderer.setColor(Assets.statisticsColor);
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

    public void addDamageProjectile(ClientShip target, float damage, float x, float y) {
        target.getCenterPos(tmp);
        float ttl = tmp.dst2(x, y) / DamageProjectile.AMMO_VELOCITY_SQUARED;
        damageProjectiles.add(new DamageProjectile(target, damage, x, y, ttl));
    }

    public void addDamageText(ClientShip target, float damage) {
        float width = target.getWidth();
        float height = target.getHeight();

        float offsetX = (float) Random.nextDouble() * width;
        float offsetY = (float) Random.nextDouble() * height;
        if (Random.nextBoolean()) {
            offsetX *= -1;
        }
        if (Random.nextBoolean()) {
            offsetY *= -1;
        }
        damageMessages.add(new DamageText(target, damage, offsetX, offsetY));
    }

    private class DamageProjectile {
        public static final float AMMO_VELOCITY_SQUARED = 1.5f * 1.5f; // in units per second

        final ClientShip target;
        final float damage;
        final long startTime;
        final float startX, startY, timeToLive;

        public DamageProjectile(ClientShip target, float damage, float startX, float startY, float timeToLive) {
            startTime = System.currentTimeMillis();
            this.target = target;
            this.damage = damage;
            this.startX = startX;
            this.startY = startY;
            this.timeToLive = timeToLive;
        }
    }

    private class DamageText {
        public static final long FADEOUT_TIME = 1000; // in ms
        final ClientShip target;
        final float damage;
        final long startTime;

        final float offsetX, offsetY;

        public DamageText(ClientShip target, float damage, float offsetX, float offsetY) {
            startTime = System.currentTimeMillis();
            this.target = target;
            this.damage = damage;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            log(offsetX + ", " + offsetY);
        }
    }

    public void setSelectionRectangleStart(float x, float y) {
        selectionRectangleStart.set(x, y, 0);
        cam.project(selectionRectangleStart);
    }

    public void setSelectionRectangleEnd(float x, float y) {
        getSelectionRectangleEnd.set(x, y, 0);
        cam.project(getSelectionRectangleEnd);
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

    public void setHoveringTarget(ClientShip hoveringTarget) {
        this.hoveringTarget = hoveringTarget;
    }
}
