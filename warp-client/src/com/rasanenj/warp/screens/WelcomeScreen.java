package com.rasanenj.warp.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.rasanenj.warp.Assets;
import com.rasanenj.warp.WarpGame;
import com.rasanenj.warp.messaging.ServerConnection;
import com.rasanenj.warp.storage.LocalStorage;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class WelcomeScreen implements Screen {
    private final ServerConnection serverConnection;
    private final WarpGame game;
    private final Stage stage;

    public WelcomeScreen(ServerConnection serverConnection, final WarpGame game) {
        this.game = game;
        this.serverConnection = serverConnection;

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        stage = new Stage(screenWidth, screenHeight, true);

        Window welcomeWindow = new Window("Welcome to Warp", Assets.skin);
        welcomeWindow.row().pad(10);
        welcomeWindow.add(new Label("Name:", Assets.skin));
        String name = loadEarlierName();
        TextField nameField;
        if (name == null) {
            nameField = new TextField("", Assets.skin);
            nameField.setMessageText("Your nick here");
        }
        else {
            nameField = new TextField(name, Assets.skin);
        }

        // TODO: only allow valid characters when entering name
        nameField.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char key) {
                if (key == '\r' || key == '\n') {
                    String name = textField.getText();
                    storeName(name);
                    // TODO: make this open connection
                    game.setScreen(WarpGame.ScreenType.BUILD_FLEET);
                }
            }
        });
        welcomeWindow.add(nameField);
        welcomeWindow.pack();

        welcomeWindow.setPosition(MathUtils.ceil((screenWidth - welcomeWindow.getWidth()) / 2f),
                MathUtils.ceil((screenHeight - welcomeWindow.getHeight()) / 2f));

        stage.addActor(welcomeWindow);
    }

    private void storeName(String name) {
        LocalStorage.store(LocalStorage.NAME, name);
    }

    private String loadEarlierName() {
        String name = LocalStorage.fetch(LocalStorage.NAME);
        return name;
    }

    @Override
    public void render(float delta) {
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
}
