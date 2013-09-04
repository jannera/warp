package com.rasanenj.warp;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.rasanenj.warp.chat.ChatHandler;
import com.rasanenj.warp.messaging.MessageDelegator;
import com.rasanenj.warp.messaging.ServerConnection;


public class WarpGame implements ApplicationListener {
    Skin skin;
    Stage stage;
    SpriteBatch batch;
    Label fpsLabel;
    final MessageDelegator delegator = new MessageDelegator();
    ServerConnection serverConnection;

    @Override
	public void create() {
        batch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("data/uiskin.json"));
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        stage = new Stage(screenWidth, screenHeight, false);
        Gdx.input.setInputProcessor(stage);

        TextField textfield = new TextField("", skin);
        textfield.setMessageText("Write your messages here");
        fpsLabel = new Label("fps:", skin);

        final Label chatMessages = new Label("test message", skin);
        chatMessages.setFillParent(true);
        chatMessages.setWrap(true);

        final ScrollPane scrollPane = new ScrollPane(chatMessages, skin);

        final int rows = 10;
        Window window = new Window("Chat", skin);
        // window.debug();
        window.setPosition( (screenWidth - 300) /2f, (screenHeight - 200) /2f);
        window.defaults().spaceBottom(10);
        window.row().fill().expandX();
        window.add(scrollPane).minWidth(300).minHeight(chatMessages.getHeight() * rows).expand().fill().colspan(2);
        window.row();
        window.add(textfield).minWidth(100).expandX().fillX().colspan(3);
        window.row();
        window.add(fpsLabel).colspan(4);
        window.pack();

        chatMessages.setText("");

        // window.setPosition((screenWidth - window.getPrefWidth()) / 2f, (screenHeight - window.getPrefHeight()) / 2f);

        stage.addActor(window);

        serverConnection = new ServerConnection("ws://localhost:8887", delegator);

        ChatHandler chatHandler = new ChatHandler(serverConnection, chatMessages, scrollPane, textfield);
        chatHandler.register(delegator);
	}

	@Override
	public void dispose() {
        stage.dispose();
        skin.dispose();
	}

	@Override
	public void render() {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

        fpsLabel.setText("fps: " + Gdx.graphics.getFramesPerSecond());

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
	}

	@Override
	public void resize(int width, int height) {
        stage.setViewport(width, height, false);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
}
