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

import java.util.logging.Level;
import java.util.logging.Logger;


public class WarpGame extends Game implements ResizeHandler {
    final MessageDelegator delegator = new MessageDelegator();
    ServerConnection serverConnection;

    ChatScreen chatScreen;
    BattleScreen battleScreen;

    @Override
	public void create() {
        Assets.load();
        Window.addResizeHandler(this);
        final String host = "ws://" + getHost() + ":" + Constants.PORT;
        serverConnection = new ServerConnection(host, delegator);
        // TODO: move this connecting to servers to Screens (or their handlers)

        chatScreen = new ChatScreen(serverConnection);
        battleScreen = new BattleScreen(serverConnection);

        // setScreen(chatScreen);
        setScreen(battleScreen);

        serverConnection.open();

        // battleScreen.createNPC(host);
    }

    @Override
    public void onResize(ResizeEvent event) {
        Gdx.graphics.setDisplayMode(scaleSize(event.getWidth()),
                scaleSize(event.getHeight()), false);
    }

    public static int scaleSize(int size) {
        return (int) (size * 0.97f);
    }

    private native String getHost() /*-{
        return window.location.hostname;
    }-*/;
}
