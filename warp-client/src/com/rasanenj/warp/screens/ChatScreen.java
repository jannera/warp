package com.rasanenj.warp.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.rasanenj.warp.chat.ChatHandler;
import com.rasanenj.warp.messaging.ServerConnection;
import com.rasanenj.warp.ui.chat.ChatWindow;

/**
 * @author gilead
 */
public class ChatScreen implements Screen {
    Stage stage;
    ChatHandler chatHandler;

    public ChatScreen(ServerConnection serverConnection) {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        stage = new Stage(screenWidth, screenHeight, true);

        ChatWindow chatWindow = new ChatWindow();

        Window window = chatWindow.getWindow();
        window.setPosition(MathUtils.ceil((screenWidth - 300) / 2f), MathUtils.ceil((screenHeight - 200) /2f));

        stage.addActor(window);

        this.chatHandler = new ChatHandler(serverConnection);
        chatHandler.setListener(chatWindow);
        chatWindow.setChatHandler(chatHandler);
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
        // stage.setViewport(width, height, false);
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
}
