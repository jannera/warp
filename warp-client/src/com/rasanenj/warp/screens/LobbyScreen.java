package com.rasanenj.warp.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.rasanenj.warp.WarpGame;
import com.rasanenj.warp.chat.ChatHandler;
import com.rasanenj.warp.entities.ShipStats;
import com.rasanenj.warp.messaging.*;
import com.rasanenj.warp.ui.chat.ChatWindow;
import com.rasanenj.warp.ui.fleetbuilding.FleetBuildWindow;
import com.rasanenj.warp.ui.fleetbuilding.ShipBuildWindow;

/**
 * @author gilead
 */
public class LobbyScreen implements Screen {
    private final ServerConnection serverConnection;
    private final WarpGame game;
    Stage stage;
    ChatHandler chatHandler;
    FleetBuildWindow currentBuild;

    public LobbyScreen (ServerConnection serverConnection, final WarpGame game) {
        this.game = game;
        this.serverConnection = serverConnection;

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        stage = new Stage(screenWidth, screenHeight, true);

        // start the Chat window
        ChatWindow chatWindow = new ChatWindow();

        Window window = chatWindow.getWindow();
        stage.addActor(window);

        this.chatHandler = new ChatHandler(serverConnection);
        chatHandler.setListener(chatWindow);
        chatWindow.setChatHandler(chatHandler);

        // start the fleet building window
        currentBuild = new FleetBuildWindow();

        stage.addActor(currentBuild.getWindow());

        currentBuild.loadCurrentBuild();

        Window buildWindow = currentBuild.getWindow();
        // NOTE: be sure to give integers as position.. otherwise the fonts might start showing up funny for some weird reason

        currentBuild.getStartFight().addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                startTestFlight();
                game.setScreen(WarpGame.ScreenType.BATTLE);
            }
        });

        // position both windows
        float widthLeft = screenWidth - window.getWidth() - buildWindow.getWidth();
        if (widthLeft < 0) {
            widthLeft = 0;
        }
        float padding = MathUtils.floor(widthLeft/3f);
        window.setPosition(padding, MathUtils.ceil((screenHeight - window.getWidth()) /2f));

        buildWindow.setPosition(MathUtils.floor(padding * 2f + window.getWidth()),
                MathUtils.ceil((screenHeight - buildWindow.getHeight()) / 2f));

        serverConnection.register(new ConnectionListener());
    }

    @Override
    public void render(float delta) {
        chatHandler.processArrivedMessages();
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

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
        stage.dispose();
    }

    public void loadEarlierBuild() {
        currentBuild.loadCurrentBuild();
    }

    public void startTestFlight() {
        serverConnection.send(new JoinBattleMessage("gilead", -1, -1)); // TODO: fetch this elsewhere

        for (ShipStats stats : currentBuild.getStats()) {
            serverConnection.send(new ShipStatsMessage(stats));
        }
    }

    private class ConnectionListener implements ServerConnection.OpenCloseListener {

        @Override
        public void onOpen() {
            if (WarpGame.START_SCREEN == WarpGame.ScreenType.LOBBY) {
                serverConnection.send(new JoinServerMessage(WelcomeScreen.loadEarlierName(), -1));
                serverConnection.send(new JoinChatMessage(WelcomeScreen.loadEarlierName()));
            }
        }

        @Override
        public void onClose() {
            // TODO: notify the user that the connection to server has been lost
        }
    }
}
