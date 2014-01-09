package com.rasanenj.warp.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.rasanenj.warp.WarpGame;
import com.rasanenj.warp.entities.ShipStats;
import com.rasanenj.warp.messaging.JoinServerMessage;
import com.rasanenj.warp.messaging.ServerConnection;
import com.rasanenj.warp.messaging.ShipStatsMessage;
import com.rasanenj.warp.ui.fleetbuilding.FleetBuildWindow;


/**
 * NOTE: ATM we're happily mixing Model, View and Controller all together in these
 * Classes below. You might want to change that at some point.
 */
public class FleetBuildingScreen implements Screen {
    private final ServerConnection serverConnection;
    private final WarpGame game;
    Stage stage;
    FleetBuildWindow currentBuild;

    private void startTestFlight() {
        serverConnection.send(new JoinServerMessage("gilead", -1, -1));

        for (ShipStats stats : currentBuild.getStats()) {
            serverConnection.send(new ShipStatsMessage(stats));
        }

        game.setScreen(WarpGame.ScreenType.BATTLE);
    }

    public FleetBuildingScreen(ServerConnection serverConnection, WarpGame warpGame) {
        this.game = warpGame;
        this.serverConnection = serverConnection;

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        stage = new Stage(screenWidth, screenHeight, true);

        currentBuild = new FleetBuildWindow();

        stage.addActor(currentBuild.getWindow());

        currentBuild.add(currentBuild.createShipFromCatalog(1));

        Window buildWindow = currentBuild.getWindow();

        buildWindow.setPosition(MathUtils.ceil((screenWidth - buildWindow.getWidth()) / 2f),
                MathUtils.ceil((screenHeight - buildWindow.getHeight()) / 2f));
        // NOTE: be sure to give integers as position.. otherwise the fonts might start showing up funny for some weird reason

        currentBuild.getStartFight().addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                startTestFlight();
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

        currentBuild.updateUI();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }
}
