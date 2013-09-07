package com.rasanenj.warp;

import com.badlogic.gdx.Game;
import com.rasanenj.warp.messaging.MessageDelegator;
import com.rasanenj.warp.messaging.ServerConnection;
import com.rasanenj.warp.screens.BattleScreen;
import com.rasanenj.warp.screens.ChatScreen;


public class WarpGame extends Game {
    final MessageDelegator delegator = new MessageDelegator();
    ServerConnection serverConnection;

    ChatScreen chatScreen;
    BattleScreen battleScreen;

    @Override
	public void create() {
        serverConnection = new ServerConnection("ws://localhost:8887", delegator);

        chatScreen = new ChatScreen(serverConnection);
        battleScreen = new BattleScreen(serverConnection);

        setScreen(chatScreen);
        //setScreen(battleScreen);

        serverConnection.open();

	}
}
