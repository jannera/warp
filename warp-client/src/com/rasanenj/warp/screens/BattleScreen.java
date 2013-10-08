package com.rasanenj.warp.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.rasanenj.warp.BattleHandler;
import com.rasanenj.warp.entities.ClientShip;
import com.rasanenj.warp.messaging.ServerConnection;
import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 *
 */
public class BattleScreen implements Screen {

    private Stage stage;
    private BattleHandler battleHandler;

    private static final int CAMERA_SIZE = 20;
    private static final float zoomStep = 0.05f;
    private float zoom = 1f;
    ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final Vector2 tmp = new Vector2(), force = new Vector2();
    private final Vector2 corners[] = new Vector2[4];

    private static final Color DODGER_BLUE = new Color(30f/255f, 191f/255f, 1, 1);

    public BattleScreen(ServerConnection conn) {
        stage = new Stage();
        stage.setViewport(CAMERA_SIZE, CAMERA_SIZE, true);

        battleHandler = new BattleHandler(this, conn);
        for (int i=0; i < 4; i++) {
            corners[i] = new Vector2();
        }
    }

    public Stage getStage() {
        return stage;
    }

    @Override
    public void render(float delta) {
        battleHandler.update(delta);
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
        renderVectors();
    }

    public void renderVectors() {
        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        for (ClientShip ship : battleHandler.getShips()) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            ship.getCenterPos(tmp);
            /*
            // render the velocity of the ship
            shapeRenderer.setColor(DODGER_BLUE);
            shapeRenderer.line(tmp.x, tmp.y,
                    tmp.x + ship.getVelocity().x, tmp.y + ship.getVelocity().y);
                    */


            // render the rectangle that limits the maximum force vector
            ship.getForceLimitCorners(corners);
            shapeRenderer.setColor(Color.YELLOW);
            for (int i=0; i < 4; i++) {
                // first we make a vector from i'th corner to i+1'th corner
                int next = i+1;
                if (next == 4) {
                    next = 0; // last vector is from first point to the last point
                }
                shapeRenderer.line(corners[i].x, corners[i].y,
                             corners[next].x, corners[next].y);
            }

            // render the ideal impulse
            shapeRenderer.setColor(Color.BLUE);
            shapeRenderer.line(tmp.x, tmp.y,
                    tmp.x + ship.getImpulseIdeal().x, tmp.y + ship.getImpulseIdeal().y);

            // render the impulse
            shapeRenderer.setColor(DODGER_BLUE);
            shapeRenderer.line(tmp.x, tmp.y,
                    tmp.x + ship.getImpulse().x, tmp.y + ship.getImpulse().y);

            shapeRenderer.end();
        }
    }

    @Override
    public void resize(int width, int height) {
        //stage.setViewport(width, height, true);
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

    public void setCameraPos(float x, float y) {
        float dx = x - stage.getCamera().position.x;
        float dy = y - stage.getCamera().position.y;
        stage.getCamera().translate(dx, dy, 0);
    }

    public void zoom(int amount) {
        zoom += zoomStep * amount;
        log("zoom:" + zoom);
        stage.setViewport(CAMERA_SIZE * zoom, CAMERA_SIZE * zoom, true);
    }
}
