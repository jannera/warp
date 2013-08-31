package com.rasanenj.warp;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.sksamuel.gwt.websockets.Websocket;
import com.sksamuel.gwt.websockets.WebsocketListener;

public class WarpGame implements ApplicationListener {
	Skin skin;
    Stage stage;
    SpriteBatch batch;

	@Override
	public void create() {
        batch = new SpriteBatch();
        stage = new Stage();
        Gdx.input.setInputProcessor(stage);

        // Create a table that fills the screen. Everything else will go inside this table.
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        skin = new Skin(Gdx.files.internal("data/uiskin.json"));

        final TextField chatInput = new TextField("asdf", skin);

        table.add(chatInput);

        final Websocket socket = new Websocket("ws://localhost:8887");

        socket.addListener(new WebsocketListener() {
            @Override
            public void onClose() {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onMessage(String msg) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onOpen() {
                socket.send("lol");
            }
        });
        socket.open();

        chatInput.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char key) {
                if (key == '\n' || key == '\r') {
                    socket.send(chatInput.getText());
                    chatInput.setText("");
                }
            }
        });
        // socket.close();
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
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
        Table.drawDebug(stage);
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
