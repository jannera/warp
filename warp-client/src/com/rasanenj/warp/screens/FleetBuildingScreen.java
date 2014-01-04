package com.rasanenj.warp.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.rasanenj.warp.entities.ShipStats;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class FleetBuildingScreen implements Screen {
    Skin skin;
    Stage stage;
    SpriteBatch batch;
    Label total;
    FleetBuild currentBuild = new FleetBuild();

    /**
     * NOTE: ATM we're happily mixing Model, View and Controller all together in these
     * PropertySliders. You might want to change that at some point.
     */
    private class PropertySlider {
        private Slider slider;
        private Label description;

        final float[] costs, values;

        final private String id;

        public PropertySlider(String id, Window window, String propertyName, float[] values, float[] costs) {
            this.id = id;
            if (values.length != costs.length) {
                throw new RuntimeException("Cost and value arrays must have same amount of items for id " + id);
            }
            this.values = values;
            this.costs = costs;
            Label label = new Label(propertyName, skin);
            window.row().fill().expandX().fillX();
            window.add(label);
            slider = new Slider(0, costs.length - 1, 1, false, skin);
            window.add(slider);
            description = new Label("", skin);
            update();
            window.add(description);
        }

        private String getId() {
            return id;
        }

        public float getCost() {
            int index = (int) slider.getValue();
            return costs[index];
        }

        public float getValue() {
            int index = (int) slider.getValue();
            return values[index];
        }

        private String getDescription() {
            return getValue() + " (" + getCost() + " pts)";
        }

        public void update() {
            description.setText(getDescription());
        }
    }

    private class ShipBuild {
        private final int typeId;

        public ShipBuild(int typeId) {
            this.typeId = typeId;
        }

        public void add(PropertySlider slider) {
            sliders.add(slider);

        }

        public ShipStats getStats() {
            return null;
        }

        public float getTotalCost() {
            float totalCost = 0;

            for (PropertySlider slider : sliders) {
                slider.update();
                totalCost += slider.getCost();
            }
            return totalCost;
        }

        private final Array<PropertySlider> sliders = new Array<PropertySlider>(false, 16);
    }

    private class FleetBuild {
        private final Array<ShipBuild> shipBuilds = new Array<ShipBuild>(false, 16);

        public float getTotalCost() {
            float totalCost = 0;
            for(ShipBuild build : shipBuilds) {
                totalCost += build.getTotalCost();
            }

            return totalCost;
        }

        public void add(ShipBuild shipBuild) {
            shipBuilds.add(shipBuild);
        }
    }

    public FleetBuildingScreen() {
        batch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("data/uiskin.json"));
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        stage = new Stage(screenWidth, screenHeight, true);

        Window window = new Window("Ship properties", skin);
        window.setPosition( (screenWidth - 300) /2f, (screenHeight - 200) /2f);

        currentBuild.add(createShipFromCatalog(window, 1));

        total = new Label("", skin);
        window.row().fill().expand();
        window.add(total).expand().fill().colspan(0);

        window.pack();

        stage.addActor(window);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

        total.setText("Total: " + currentBuild.getTotalCost());

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

    }

    private ShipBuild createShipFromCatalog(Window window, int shipTypeId) {
        JsonReader reader = new JsonReader();
        JsonValue catalog = reader.parse(Gdx.files.internal("data/shipCatalog.json"));

        JsonValue shipTypes = catalog.require("shipTypes");

        for (JsonValue shipType : shipTypes) {
            int id = shipType.getInt("id");
            if (id != shipTypeId) {
                continue;
            }

            String typeName = shipType.getString("typeName");
            window.row().fill().expandX().fillX();
            window.add(new Label("Type: " + typeName, skin));

            ShipBuild build = new ShipBuild(id);

            JsonValue categories = shipType.require("categories");
            for (JsonValue category : categories) {
                window.row().fill().expandX();
                window.add(new Label(category.getString("name"), skin));

                JsonValue properties = category.require("properties");
                for (JsonValue property : properties) {
                    JsonValue values = property.require("values");
                    final float[] valueArr = new float[values.size];
                    final float[] costArr = new float[values.size];
                    int i = 0;
                    for (JsonValue value : values) {
                        valueArr[i] = value.getFloat("value");
                        costArr[i++] = value.getFloat("cost");
                    }
                    build.add(new PropertySlider(property.getString("id"), window,
                            property.getString("name"), valueArr, costArr));
                }
            }
            return build;
        }

        return null;
    }
}
