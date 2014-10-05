package com.rasanenj.warp.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Random;
import com.rasanenj.warp.*;
import com.rasanenj.warp.actors.TiledImage;
import com.rasanenj.warp.chart.Chart;
import com.rasanenj.warp.actors.ClientShip;
import com.rasanenj.warp.entities.ShipStats;
import com.rasanenj.warp.messaging.Player;
import com.rasanenj.warp.messaging.ServerConnection;
import com.rasanenj.warp.projecting.PositionProjection;
import com.rasanenj.warp.systems.ShipShooting;
import com.rasanenj.warp.tasks.PathPlotterTask;
import com.rasanenj.warp.ui.fleetbuilding.FleetBuildWindow;


import static com.rasanenj.warp.Log.log;

/**
 * Handles rendering and Screen events.
 *
 * @author gilead
 */
public class BattleScreen implements Screen {

    private final Matrix4 normalProjection;
    private final TiledImage backgroundImage;
    private final WarpGame game;
    private final Stage stage, uiStage;
    private final Label hoveringText;
    private final FleetBuildWindow deployWindow;
    private Label textLog;
    private ScrollPane textLogScroll;
    private BattleHandler battleHandler;

    private static final int CAMERA_SIZE = 20;
    private final static float GRID_SIZE = 8;
    private final static float NAVIGATION_TARGET_SIZE = 60f;
    private final static float ACTIVE_FIRING_TARGET_SIZE = 60f;
    private final static float ACTIVE_ORBIT_TARGET_SIZE = 60f;
    private final static float HOVERING_FIRING_TARGET_SIZE = 90f;
    ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final Vector2 tmp2_1 = new Vector2(), force = new Vector2(),
        tmp2_2 = new Vector2(), tmp2_3 = new Vector2(), tmp2_4 = new Vector2();
    private final Vector2 corners[] = new Vector2[4];
    private static final int CIRCLE_SEGMENTS = 64;

    private static final float SCORE_GATHERING_POINT_SIZE = 5f;

    private static final float PROJECTILE_SIZE = 3f;

    final BitmapFont font;
    private NumberFormat twoFullDecimals = NumberFormat.getFormat("0.00");
    private NumberFormat threeFullDecimals = NumberFormat.getFormat("0.000");
    private NumberFormat noDecimals = NumberFormat.getFormat("0");

    private final Array<DamageText> damageMessages = new Array(false, 16);
    private final Array<DamageText> removables = new Array<DamageText>(false, 16);

    private final Array<DamageProjectile> damageProjectiles = new Array(false, 16);
    private final Array<DamageProjectile> removableProjectiles = new Array(false, 16);
    private final Array<LaserBeam> laserBeams = new Array(false, 16);
    private final Array<LaserBeam> removableLaserBeams = new Array(false, 16);
    private final OrthographicCamera cam;
    private boolean selectionRectangleActive = false;
    private final Vector3 selectionRectangleStart = new Vector3(), getSelectionRectangleEnd = new Vector3();

    // only for drawing directly on the screen, like rendering text in screen coordinates
    private final SpriteBatch batch = new SpriteBatch();

    private OptimalRenderingState optimalRenderingState = OptimalRenderingState.NOTHING;
    private Vector2 manualSteeringEnd;
    private Vector2 manualSteeringStart;
    private boolean manualSteeringActive = false;

    private ClientShip hoveringTarget = null;

    private final Array<PathPlotterTask> plotters = new Array<PathPlotterTask>(false, 0);
    private OrbitUIHandler orbitUIHandler;

    final Table hoveringTable;
    final ActorBrowsingWindow debugWindow;
    private long countDownZero = 0;

    public BattleScreen(final ServerConnection conn, LobbyScreen lobbyScreen, WarpGame game) {
        this.game = game;
        stage = new Stage();
        stage.setViewport(CAMERA_SIZE, CAMERA_SIZE, true);
        cam = (OrthographicCamera) stage.getCamera();

        font = Assets.skin.getFont("default-font");

        // init ui actors that are drawn over the game layer
        uiStage = new Stage();
        uiStage.setViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        deployWindow = new FleetBuildWindow(true);
        deployWindow.loadCurrentBuild();
        float y = (uiStage.getHeight() - deployWindow.getWindow().getHeight()) / 2f;
        deployWindow.getWindow().setY(y);
        uiStage.addActor(deployWindow.getWindow());

        initTextLog();

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
        cam.zoom = 3.5f;

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.6f, 0.6f, 0.6f, 0.5f);
        pixmap.fill();

        TextureRegionDrawable textureRegionDrawable = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));

        hoveringTable = new Table(Assets.skin);
        hoveringTable.row().fill().pad(3f);
        hoveringTable.setBackground(textureRegionDrawable);
        hoveringTable.setColor(0.6f, 0.6f, 0.6f, 0.75f);
        uiStage.addActor(hoveringTable);

        hoveringText = new Label(getHoverInfo(0L, 0f, 0f), Assets.skin);
        hoveringText.setColor(Color.WHITE);
        hoveringTable.add(hoveringText);
        hoveringTable.pack();
        debugWindow = new ActorBrowsingWindow("debug", uiStage);

        batch.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
    }

    private void initTextLog() {
        textLog = new Label("", Assets.skin);
        // textLog.setAlignment(Align.top, Align.left);
        textLog.setHeight(30);
        textLog.setFillParent(false);
        textLog.setWrap(true);

        textLogScroll = new ScrollPane(textLog, Assets.skin);


        Window textLogWindow = new Window("Log", Assets.skin);
        textLogWindow.row().fill().expandX();
        textLogWindow
                .add(textLogScroll)
                .minWidth(500).minHeight(500).expand().fill();
        textLogWindow.pack();

        float x = uiStage.getWidth() - textLogWindow.getWidth();
        float y = (uiStage.getHeight() - textLogWindow.getHeight()) / 2f;
        textLogWindow.setPosition(x, y);

        uiStage.addActor(textLogWindow);
    }


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

    public void addLaserBeam(ClientShip shooter, ClientShip target, boolean hit) {
        shooter.getCenterPos(tmp2_1);
        float startX = tmp2_1.x, startY = tmp2_1.y;
        target.getCenterPos(tmp2_1);
        addLaserBeam(startX, startY, tmp2_1.x, tmp2_1.y, Assets.getHiliteColor(shooter.getOwner()), hit);
    }

    public void addScoreGatheringPoint(float x, float y) {
        Image point = new Image(Assets.scoreGatheringPoint);
        point.setColor(Color.GRAY);
        point.setSize(SCORE_GATHERING_POINT_SIZE, SCORE_GATHERING_POINT_SIZE);
        point.setPosition(x - SCORE_GATHERING_POINT_SIZE/2f, y - SCORE_GATHERING_POINT_SIZE/2f);
        stage.addActor(point);
    }

    public void setCountdown(int lengthInSec) {
        if (lengthInSec != 0) {
            this.countDownZero = System.currentTimeMillis() + 1000 * lengthInSec;
        }
        else {
            countDownZero = 0;
        }
    }

    private Array<DeployWarning> deployWarnings = new Array<DeployWarning>(false, 16);
    private Array<DeployWarning> removableDeployWarnings = new Array<DeployWarning>(false, 16);

    public void addDeployWarning(float x, float y, float fleetSize, int msUntil, Player player) {
        deployWarnings.add(new DeployWarning(x, y, fleetSize, System.currentTimeMillis() + msUntil, player));
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
        updateLasers();
        updateDeployWarnings();
        stage.act(Gdx.graphics.getDeltaTime());
        estimateShipPositions();
        stage.draw();

        renderPositionProjections();

        renderPaths();
        renderVectors();
        renderOffScreenShips();
        renderTargetValueCircles();
        renderProjectiles();
        renderLasers();
        renderSelectionCircles();
        renderHealthBars();

        renderDeployWarnings();

        // render active commands for selected ships
        renderOrbitTargets();
        renderNavigationTargets();

        renderOptimals();
        renderOrbitCircle();
        renderDamageTexts();
        // renderShipTexts();

        renderWeaponCooldowns();

        renderManualSteering();
        renderDebugText();
        renderSelectionRectangle();
        renderPhysicsVertices();

        renderScore();

        renderTextBelowMouseCursor();

        // renderOrigins();

        updateHoverTable();
        uiStage.act(Gdx.graphics.getDeltaTime());
        uiStage.draw();
    }

    private void renderOrigins() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);
        for (ClientShip s : battleHandler.getShips()) {
            s.getOriginPos(tmp2_1);
            shapeRenderer.circle(tmp2_1.x, tmp2_1.y, 0.5f);
        }
        shapeRenderer.end();
    }

    private void updateDeployWarnings() {
        removableDeployWarnings.clear();
        long currentTime = System.currentTimeMillis();
        for (DeployWarning w : deployWarnings) {
            if (w.getEstimatedTime() <= currentTime) {
                removableDeployWarnings.add(w);
            }
        }
        deployWarnings.removeAll(removableDeployWarnings, true);
    }

    private static final float DEFAULT_TIME_DEPLOY_WARNING_CIRCLE_RADIUS_PX = 50f;
    private void renderDeployWarnings() {
        batch.begin();
        for (DeployWarning w : deployWarnings) {
            long until = w.getEstimatedTime() - System.currentTimeMillis();

            if (until <= 0) {
                continue;
            }

            float radius = DEFAULT_TIME_DEPLOY_WARNING_CIRCLE_RADIUS_PX
                    * (until/1000f/Constants.DEPLOY_TIME_SECONDS);
            tmp3.set(w.getX(), w.getY(), 0);
            cam.project(tmp3);
            tmp3.x -= radius;
            tmp3.y -= radius;
            batch.setColor(Assets.getBasicColor(w.getOwner()));
            // log(Assets.getBasicColor(w.getOwner()).r + ", " + Assets.getBasicColor(w.getOwner()).a);
            batch.draw(Assets.scoreGatheringPoint, tmp3.x, tmp3.y, radius * 2f, radius * 2f);
        }
        batch.end();
    }

    private void renderScore() {
        Array<Player> players = battleHandler.getPlayers();
        if (players.size == 0) {
            return;
        }

        int lines = players.size + 1;
        String[] scores = new String[lines];
        String full = "";

        if (countDownZero == 0) {
            scores[0] = "";
        }
        else {
            int secondsLeft = (int) ((countDownZero - System.currentTimeMillis()) / 1000);
            secondsLeft = Math.max(0, secondsLeft);
            scores[0] = Integer.toString(secondsLeft);
        }

        full = scores[0];

        for (int i=0; i < players.size; i++) {
            Player p = players.get(i);
            String name = p.getName();
            if (name.isEmpty()) {
                name = Long.toString(p.getId());
            }
            scores[i+1] = name + " : " + noDecimals.format(p.getScore());
            full += "\n" + scores[i+1];
        }

        BitmapFont.TextBounds bounds = font.getMultiLineBounds(full);
        float singleHeight = bounds.height / lines;

        float x = Gdx.graphics.getWidth() - bounds.width - 5f;
        float startY = singleHeight * lines + 5f;

        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, scores[0], x, startY);
        for (int i=0; i < players.size; i++) {
            Player p = players.get(i);
            font.setColor(Assets.getBasicColor(p));
            float y = startY - singleHeight * (i + 1);
            font.draw(batch, scores[i + 1], x, y);
        }
        batch.end();

    }

    private void updateHoverTable() {
        hoveringTable.setVisible(hoveringTarget != null);
        if (hoveringTarget == null) {
            return;
        }

        // position the table left of the ship, in the center
        hoveringText.setText(getHoverInfo(hoveringTarget));
        hoveringText.setAlignment(Align.left);
        hoveringTarget.getCenterPos(tmp2_1);
        tmp3.set(hoveringTarget.getLeftX(), tmp2_1.y, 0);
        cam.project(tmp3);

        tmp3.x -= hoveringTable.getWidth();
        tmp3.x -= 5f; // padding
        tmp3.y -= hoveringTable.getHeight() / 2f;
        hoveringTable.setPosition(tmp3.x, tmp3.y);
    }

    private String getHoverInfo(ClientShip s) {
        final ShipStats stats = s.getStats();
        return getHoverInfo(s.getId(), stats.getMaxLinearVelocity(),
                stats.getWeaponTracking());
    }

    private String getHoverInfo(Long id, float maxVel, float tracking) {
        String result = "";
        result += "id " + id;
        result += "\n" + "maxvel " + twoFullDecimals.format(maxVel);
        result += "\n" + "tracking " + threeFullDecimals.format(tracking);
        return result;
    }

    private void renderTargetValueCircles() {
        batch.begin();
        long myid = battleHandler.getMyId();
        font.setColor(Color.YELLOW);
        for (ClientShip ship: battleHandler.getShips()) {
            if (ship.getOwner().getId() == myid) {
                continue;
            }

            String value = ship.getTargetValue().toString();
            if (value == "") {
                continue;
            }

            Vector2 pos = getTargetValueStartPosPx(ship, value);

            font.draw(batch, value, pos.x, pos.y);
        }
        batch.end();
    }

    private Vector2 getTargetValueStartPosPx(ClientShip ship, String value) {
        ship.getCenterPos(tmp2_1);
        tmp2_1.y += ship.getHeight();
        tmp3.set(tmp2_1.x, tmp2_1.y, 0);
        cam.project(tmp3);

        // now we have window coordinates just above the ship vertically and right in middle of the ship horizontally
        BitmapFont.TextBounds bounds = font.getBounds(value);
        tmp2_1.set(tmp3.x - bounds.width / 2f, tmp3.y + bounds.height);
        return tmp2_1;
    }

    final static float weaponCoolDownCircleRadius = 10f;

    private void renderWeaponCooldowns() {
        shapeRenderer.setProjectionMatrix(normalProjection);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (ClientShip ship : battleHandler.getShips()) {
            float readiness = ship.getFiringReadiness();

            if (readiness < 1f) {
                readiness *= 360f;

                tmp3.set(ship.getX(), ship.getY() + ship.getHeight(), 0);
                cam.project(tmp3);
                tmp3.y += 10 + weaponCoolDownCircleRadius;
                tmp3.x -= (10 + weaponCoolDownCircleRadius);
                shapeRenderer.setColor(Color.WHITE);
                shapeRenderer.arc(tmp3.x, tmp3.y, weaponCoolDownCircleRadius, 360f - readiness + 90f, readiness);
            }
        }
        shapeRenderer.end();
        shapeRenderer.setProjectionMatrix(cam.combined);
    }

    private void renderPositionProjections() {
        if (!Settings.renderPositionProjections) {
            return;
        }

        shapeRenderer.setProjectionMatrix(normalProjection);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        Color c = new Color();

        for (ClientShip ship : battleHandler.getShips()) {
            c.set(ship.getCurrentColor());
            for (PositionProjection projection : ship.getProjectedPositions()) {
                float scale = 1f - 0.75f * (float) projection.getTimestamp() / (float) ShipShooting.PROJECTION_TIME_MS;
                shapeRenderer.setColor(scale * c.r, scale * c.g, scale * c.b, 1);
                tmp3.set(projection.getPosition().x, projection.getPosition().y, 0);
                cam.project(tmp3);
                shapeRenderer.circle(tmp3.x, tmp3.y, 3);
                shapeRenderer.line(tmp3.x, tmp3.y, tmp3.x + projection.getVelocity().x * 3f, tmp3.y + projection.getVelocity().y * 3f);
            }
        }
        shapeRenderer.end();
        shapeRenderer.setProjectionMatrix(cam.combined);
    }

    private void estimateShipPositions() {
        if (battleHandler.getState() == GameState.PAUSED) {
            return;
        }
        final long timeNow = System.currentTimeMillis();
        for (ClientShip ship : battleHandler.getShips()) {
            ship.updatePos(timeNow);
        }
    }

    final Array<Float> chances = new Array<Float>(false, 16);

    private void renderTextBelowMouseCursor() {
        if (orbitUIHandler.getState() != OrbitUIHandler.State.SELECTING_RADIUS) {
            return;
        }

        float orbitCircleRadius = orbitUIHandler.getOrbitRadius();
        float orbitDst2 = orbitCircleRadius * orbitCircleRadius;

        tmp2_1.set(0, 0); // shooter pos
        tmp2_2.set(orbitCircleRadius, 0); // target pos
        tmp2_4.set(0, 0); // shooter lin vel

        chances.clear();
        ShipStats shooterStats = orbitUIHandler.getOrbitTargetShip().getStats();
        for (ClientShip s : battleHandler.getSelectedShips()) {
            float vel = s.getStats().getOrbitVelocities().getVelocity(orbitDst2);
            tmp2_4.set(0, vel); // target lin vel

            float chance = DamageModeler.getChance(tmp2_1, tmp2_2, tmp2_3, tmp2_4, shooterStats, s.getStats()) * 100f;
            if (!chances.contains(chance, false)) {
                chances.add(chance);
            }
        }

        String textBelowCursor = "";
        for (Float f : chances) {
            if (!textBelowCursor.isEmpty()) {
                textBelowCursor += ", ";
            }
            textBelowCursor += noDecimals.format(f);
        }
        textBelowCursor += "%";

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
        target.getCenterPos(tmp2_1);
        tmp3.set(tmp2_1.x, tmp2_1.y, 0);
        cam.project(tmp3);
        batch.draw(tex, tmp3.x - halfSize, tmp3.y - halfSize, size, size);
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
        orbitCircleTarget.getCenterPos(tmp2_1);
        shapeRenderer.circle(tmp2_1.x, tmp2_1.y, orbitCircleRadius);
        shapeRenderer.end();
    }

    private void renderSelectionCircles() {
        batch.begin();

        for (ClientShip ship : battleHandler.getShips()) {
            if (!ship.isCircled()) {
                continue;
            }
            ship.getCenterPos(tmp2_1);
            tmp3.set(tmp2_1.x, tmp2_1.y, 0);
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

    private static final float LASER_WIDTH = 30f;
    private static final float LASER_MAX_CAP_LENGTH = 50f;

    private static final Color tmpColor = new Color();

    private void renderLasers() {
        if (laserBeams.size == 0) {
            return;
        }
        long timeNow = System.currentTimeMillis();
        batch.begin();

        for (LaserBeam laser : laserBeams) {
            tmp3.set(laser.startX, laser.startY, 0);
            tmp3end.set(laser.endX, laser.endY, 0);
            cam.project(tmp3);
            cam.project(tmp3end);
            tmp3end.sub(tmp3);
            tmp2_1.set(tmp3end.x, tmp3end.y);
            float angle = tmp2_1.angle() + 270f;
            float height = tmp2_1.len();

            float capLength = height / 3f;
            if (capLength > LASER_MAX_CAP_LENGTH) {
                capLength = LASER_MAX_CAP_LENGTH;
            }

            float fade = 1f- (float) Math.pow((timeNow - laser.startTime) / laser.getMaxDuration(), 2);

            renderLaser(Assets.laserStartBackground, tmp3.x, tmp3.y, angle, capLength, laser.baseColor, fade);
            if (laser.hit) {
                renderLaser(Assets.laserStartOverlay   , tmp3.x, tmp3.y, angle, capLength, Color.WHITE, fade);
            }

            tmp2_1.clamp(0f, height - capLength);
            renderLaser(Assets.laserEndBackground, tmp3.x + tmp2_1.x, tmp3.y + tmp2_1.y, angle, capLength, laser.baseColor, fade);
            if (laser.hit) {
                renderLaser(Assets.laserEndOverlay   , tmp3.x + tmp2_1.x, tmp3.y + tmp2_1.y, angle, capLength, Color.WHITE, fade);
            }


            tmp2_1.clamp(0f, capLength);
            renderLaser(Assets.laserMidBackground, tmp3.x + tmp2_1.x, tmp3.y + tmp2_1.y, angle, height - 2f*capLength, laser.baseColor, fade);
            if (laser.hit) {
                renderLaser(Assets.laserMidOverlay   , tmp3.x + tmp2_1.x, tmp3.y + tmp2_1.y, angle, height - 2f*capLength, Color.WHITE, fade);
            }
        }
        batch.end();
    }

    private void renderLaser(Texture t, float startX, float startY, float angle, float height, Color baseColor, float fade) {
        final float originX = Assets.laserMidBackground.getWidth() / 2f;
        final float originY = Assets.laserMidBackground.getHeight() / 2f;

        tmpColor.set(baseColor.r, baseColor.g, baseColor.b, fade);
        batch.setColor(tmpColor);
        batch.draw(t, startX, startY, originX, originY, LASER_WIDTH, height, 1f, 1f, angle,
                0, 0, t.getWidth(), t.getHeight(), false, false);
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
                addDamageText(projectile.target, projectile.damage, projectile.chance);
            }
        }

        damageProjectiles.removeAll(removableProjectiles, true);
    }

    private void updateLasers() {
        removableLaserBeams.clear();
        long timeNow = System.currentTimeMillis();
        for (LaserBeam laser : laserBeams) {
            if (timeNow - laser.startTime > laser.getMaxDuration()) {
                removableLaserBeams.add(laser);
            }
        }

        laserBeams.removeAll(removableLaserBeams, true);
    }

    private void renderNavigationTargets() {
        batch.begin();
        batch.setColor(Color.WHITE);
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
        if (hoveringTarget != null) {
            // always render the optimal of the ship under cursor
            renderOptimals(hoveringTarget);
        }

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
        ship.getCenterPos(tmp2_1);
        float optimal = ship.getStats().getWeaponOptimal();
        float falloff = ship.getStats().getWeaponFalloff();
        shapeRenderer.setColor(Color.ORANGE);
        shapeRenderer.circle(tmp2_1.x, tmp2_1.y, optimal, CIRCLE_SEGMENTS);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.circle(tmp2_1.x, tmp2_1.y, optimal + falloff, CIRCLE_SEGMENTS);
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
            String output;
            float offset = 0;
            float fade = 1f - (timeNow - damageText.startTime) / (float) DamageText.FADEOUT_TIME;
            tmp3.set(damageText.target.getX() + damageText.offsetX, damageText.target.getY()
                    - damageText.target.getHeight() * 1.5f + damageText.offsetY, 0);
            cam.project(tmp3);

            if (damageText.damage > 0 ) {
                font.setColor(fade, 0, 0, fade);
                output = twoFullDecimals.format(damageText.damage);

                // String output = String.format("%f.2", damageText.damage);
                font.draw(batch, output, tmp3.x, tmp3.y);
                offset = font.getBounds(output).width + 2;
            }

            output = noDecimals.format(damageText.chance * 100f) + "%";
            font.setColor(Color.YELLOW.r, Color.YELLOW.g, Color.YELLOW.b, fade);
            font.draw(batch, output, tmp3.x + offset, tmp3.y);
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
            ship.getCenterPos(tmp2_1);

            if (tmp2_1.x < cameraLeft) {
                left = true;
            }
            else if (tmp2_1.x > cameraRight) {
                right = true;
            }

            if (tmp2_1.y > cameraTop) {
                top = true;
            }
            else if (tmp2_1.y < cameraBottom) {
                bottom = true;
            }

            tmp2_1.x = MathUtils.clamp(tmp2_1.x, cameraLeft, cameraRight);
            tmp2_1.y = MathUtils.clamp(tmp2_1.y, cameraBottom, cameraTop);

            if (top) {
                corners[0].set(tmp2_1.x - TRIANGLE_SIDE / 2f, tmp2_1.y - TRIANGLE_SIDE);
                corners[1].set(tmp2_1.x + TRIANGLE_SIDE / 2f, tmp2_1.y - TRIANGLE_SIDE);
            }
            else if (bottom) {
                corners[0].set(tmp2_1.x - TRIANGLE_SIDE / 2f, tmp2_1.y + TRIANGLE_SIDE);
                corners[1].set(tmp2_1.x + TRIANGLE_SIDE / 2f, tmp2_1.y + TRIANGLE_SIDE);
            }
            else if (right) {
                corners[0].set(tmp2_1.x - TRIANGLE_SIDE, tmp2_1.y + TRIANGLE_SIDE / 2f);
                corners[1].set(tmp2_1.x - TRIANGLE_SIDE, tmp2_1.y - TRIANGLE_SIDE / 2f);
            }
            else if (left) {
                corners[0].set(tmp2_1.x + TRIANGLE_SIDE, tmp2_1.y + TRIANGLE_SIDE / 2f);
                corners[1].set(tmp2_1.x + TRIANGLE_SIDE, tmp2_1.y - TRIANGLE_SIDE / 2f);
            }

            shapeRenderer.setColor(ship.getCurrentColor());
            shapeRenderer.triangle(tmp2_1.x, tmp2_1.y, corners[0].x, corners[0].y, corners[1].x, corners[1].y);
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
            ship.getCenterPos(tmp2_1);

            if (Settings.renderShipVelocity) {
                shapeRenderer.setColor(Assets.statisticsColor);
                shapeRenderer.line(tmp2_1.x, tmp2_1.y,
                        tmp2_1.x + ship.getVelocity().x, tmp2_1.y + ship.getVelocity().y);
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
                shapeRenderer.line(tmp2_1.x, tmp2_1.y,
                        tmp2_1.x + ship.getImpulseIdeal().x, tmp2_1.y + ship.getImpulseIdeal().y);
            }

            if (Settings.renderImpulse) {
                shapeRenderer.setColor(Assets.statisticsColor);
                shapeRenderer.line(tmp2_1.x, tmp2_1.y,
                        tmp2_1.x + ship.getImpulse().x, tmp2_1.y + ship.getImpulse().y);

            }
        }
        shapeRenderer.end();
    }

    private void renderDebugText() {
        batch.begin();
        font.setColor(1, 1, 1, 1);
        String debugText = Gdx.graphics.getFramesPerSecond() + " fps, " + battleHandler.getShips().size + " ships";
        font.draw(batch, debugText, 2, 20);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        //stage.setViewport(width, height, true);
    }

    @Override
    public void show() {
        InputMultiplexer plexer = new InputMultiplexer(uiStage, stage);
        Gdx.input.setInputProcessor(plexer);
        Chart.init();
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

    public void addDamageProjectile(ClientShip target, float damage, float x, float y, float chance) {
        target.getCenterPos(tmp2_1);
        float ttl = tmp2_1.dst2(x, y) / DamageProjectile.AMMO_VELOCITY_SQUARED;
        damageProjectiles.add(new DamageProjectile(target, damage, ttl, x, y, chance));
    }

    public void addDamageText(ClientShip target, float damage, float chance) {
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
        damageMessages.add(new DamageText(target, damage, offsetX, offsetY, chance));
    }

    private class DamageProjectile {
        public static final float AMMO_VELOCITY_SQUARED = 1.5f * 1.5f; // in units per second

        final ClientShip target;
        final float damage, chance;
        final long startTime;
        final float startX, startY, timeToLive;

        public DamageProjectile(ClientShip target, float damage, float timeToLive, float startX, float startY, float chance) {
            this.chance = chance;
            startTime = System.currentTimeMillis();
            this.target = target;
            this.damage = damage;
            this.startX = startX;
            this.startY = startY;
            this.timeToLive = timeToLive;
        }
    }

    public void addLaserBeam(float startX, float startY, float endX, float endY, Color color, boolean hit) {
        laserBeams.add(new LaserBeam(startX, startY, endX, endY, color, hit));
    }

    private class LaserBeam {
        public static final long FADE_OUT_MS = 500;

        final float startX, startY, endX, endY;
        final long startTime;
        final Color baseColor;
        final boolean hit;

        private LaserBeam(float startX, float startY, float endX, float endY, Color color, boolean hit) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.startTime = System.currentTimeMillis();
            this.baseColor = color;
            this.hit = hit;
        }

        public float getMaxDuration() {
            if (hit) {
                return FADE_OUT_MS * 2f;
            }
            return FADE_OUT_MS;
        }
    }

    private class DamageText {
        public static final long FADEOUT_TIME = 2000; // in ms
        final ClientShip target;
        final float damage, chance;
        final long startTime;

        final float offsetX, offsetY;

        public DamageText(ClientShip target, float damage, float offsetX, float offsetY, float chance) {
            startTime = System.currentTimeMillis();
            this.target = target;
            this.damage = damage;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.chance = chance;
            // log(offsetX + ", " + offsetY);
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

    public WarpGame getGame() {
        return game;
    }

    public ActorBrowsingWindow getDebugWindow() {
        return debugWindow;
    }

    public FleetBuildWindow getDeployWindow() {
        return deployWindow;
    }

    public void addToLog(String s) {
        CharSequence existing = textLog.getText();
        String newLog = "" + existing;
        if (!newLog.isEmpty()) {
            newLog += "\n";
        }
        newLog += s;
        textLog.setText(newLog);
        textLogScroll.setScrollPercentY(1);
    }
}
