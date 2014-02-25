package com.rasanenj.warp;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.rasanenj.warp.messaging.MessageDelegator;
import com.rasanenj.warp.messaging.ServerConnection;
import com.rasanenj.warp.screens.*;

import java.util.logging.Level;
import java.util.logging.Logger;


public class WarpGame extends Game implements ResizeHandler {
    final MessageDelegator delegator = new MessageDelegator();
    ServerConnection serverConnection;

    BattleScreen battleScreen;
    WelcomeScreen welcomeScreen;
    LobbyScreen lobbyScreen;

    public enum ScreenType {
        BATTLE, LOBBY, WELCOME
    }

    @Override
	public void create() {
        Assets.load();
        Window.addResizeHandler(this);
        final String host = Utility.getHost();
        serverConnection = new ServerConnection(host, delegator);
        // TODO: move this connecting to servers to Screens (or their handlers)

        welcomeScreen = new WelcomeScreen(serverConnection, this);

        lobbyScreen = new LobbyScreen(serverConnection, this);

        battleScreen = new BattleScreen(serverConnection, lobbyScreen);

        setScreen(START_SCREEN);

        serverConnection.open();
    }

    @Override
    public void onResize(ResizeEvent event) {
        // Gdx.graphics.setDisplayMode(scaleSize(event.getWidth()), scaleSize(event.getHeight()), false);
    }

    public static int scaleSize(int size) {
        return (int) (size * 0.97f);
    }

    public void setScreen(ScreenType type) {
        switch (type) {
            case LOBBY:
                setScreen(lobbyScreen);
                break;
            case BATTLE:
                setScreen(battleScreen);
                break;
            case WELCOME:
                setScreen(welcomeScreen);
        }
    }

    public static final ScreenType START_SCREEN = ScreenType.LOBBY;
}
