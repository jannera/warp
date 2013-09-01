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
    Label fpsLabel;


    @Override
	public void create() {
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
            }
        });
        socket.open();

        batch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("data/uiskin.json"));
        stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        Gdx.input.setInputProcessor(stage);

        TextField textfield = new TextField("", skin);
        textfield.setMessageText("Write your messages here");
        fpsLabel = new Label("fps:", skin);

        Window window = new Window("Chat", skin);
        window.debug();
        window.setPosition(0, 0);
        window.defaults().spaceBottom(10);
        window.row().fill().expandX();
        window.add(textfield).minWidth(100).expandX().fillX().colspan(3);
        window.row();
        window.add(fpsLabel).colspan(4);
        window.pack();

        stage.addActor(window);

        textfield.setTextFieldListener(new TextField.TextFieldListener() {
            public void keyTyped (TextField textField, char key) {
                if (key == '\n' || key == '\r') {
                    socket.send(textField.getText());
                    textField.setText("");
                }
            }
        });
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
