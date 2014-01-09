package com.rasanenj.warp;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.rasanenj.warp.messaging.MessageDelegator;
import com.rasanenj.warp.messaging.ServerConnection;
import com.rasanenj.warp.screens.BattleScreen;
import com.rasanenj.warp.screens.ChatScreen;
import com.rasanenj.warp.screens.FleetBuildingScreen;
import com.rasanenj.warp.screens.WelcomeScreen;

import java.util.logging.Level;
import java.util.logging.Logger;


public class WarpGame extends Game implements ResizeHandler {
    final MessageDelegator delegator = new MessageDelegator();
    ServerConnection serverConnection;

    ChatScreen chatScreen;
    BattleScreen battleScreen;
    FleetBuildingScreen fleetBuildingScreen;
    WelcomeScreen welcomeScreen;

    public enum ScreenType {
        CHAT, BATTLE, BUILD_FLEET
    }

    @Override
	public void create() {
        Assets.load();
        Window.addResizeHandler(this);
        final String host = Utility.getHost();
        serverConnection = new ServerConnection(host, delegator);
        // TODO: move this connecting to servers to Screens (or their handlers)

        chatScreen = new ChatScreen(serverConnection);
        battleScreen = new BattleScreen(serverConnection);
        fleetBuildingScreen = new FleetBuildingScreen(serverConnection, this);
        welcomeScreen = new WelcomeScreen(serverConnection, this);

        // setScreen(welcomeScreen);
        setScreen(chatScreen);
        // setScreen(battleScreen);

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
            case CHAT:
                setScreen(chatScreen);
                break;
            case BATTLE:
                setScreen(battleScreen);
                break;
            case BUILD_FLEET:
                setScreen(fleetBuildingScreen);
                break;
        }
    }
}
