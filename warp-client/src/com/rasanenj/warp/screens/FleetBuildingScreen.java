package com.rasanenj.warp.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.rasanenj.warp.WarpGame;
import com.rasanenj.warp.entities.ShipStats;
import com.rasanenj.warp.messaging.JoinServerMessage;
import com.rasanenj.warp.messaging.ServerConnection;
import com.rasanenj.warp.messaging.ShipStatsMessage;
import com.rasanenj.warp.storage.LocalStorage;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static com.rasanenj.warp.Log.log;

/**
 * NOTE: ATM we're happily mixing Model, View and Controller all together in these
 * Classes below. You might want to change that at some point.
 */
public class FleetBuildingScreen implements Screen {
    private final ServerConnection serverConnection;
    private final WarpGame game;
    Skin skin;
    Stage stage;
    SpriteBatch batch;
    FleetBuild currentBuild;
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

        public int getIndex() {
            return (int) slider.getValue();
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

        public void setIndex(int index) {
            if (index < values.length && index >= 0) {
                slider.setValue(index);
            }
        }
    }

    private class ShipBuild {
        private final int typeId;
        private final Window window;
        private Label total;

        public ShipBuild(int typeId) {
            window = new Window("Ship properties", skin);
            window.setMovable(false);
            this.typeId = typeId;
        }

        public void updateUI() {
            total.setText("Total: " + getTotalCost());
        }

        public void add(PropertySlider slider) {
            sliders.add(slider);

        }

        public void addTotal() {
            total = new Label("", skin);
            window.row().fill().expand();
            window.add(total).expand().fill().colspan(0);
        }

        private HashMap<String, Float> getValues() {
            HashMap<String, Float> values = new HashMap<String, Float>(sliders.size);
            for (PropertySlider slider : sliders) {
                values.put(slider.getId(), slider.getValue());
            }
            return values;
        }

        public HashMap<String, Integer> getSliders() {
            HashMap<String, Integer> values = new HashMap<String, Integer>(sliders.size);
            for (PropertySlider slider : sliders) {
                values.put(slider.getId(), slider.getIndex());
            }
            return values;
        }

        public void setSliders(HashMap<String, Integer> indexes) {
            for (PropertySlider slider : sliders) {
                String id = slider.getId();
                Integer index = indexes.get(id);
                if (index == null) {
                    continue;
                }
                slider.setIndex(index);
            }
        }

        public ShipStats getStats() {
            HashMap<String, Float> values = getValues();

            float mass = 0; // atm fixed in server code
            float inertia = 0; // atm fixed in server code
            float force = 0; // atm derived from the acceleration

            float maxHealth = getValue(values, "maxHealth");
            float maxVelocity = getValue(values, "maxVelocity");
            float maxAcceleration = getValue(values, "maxAcceleration");
            float maxAngularVelocity = getValue(values, "maxAngularVelocity");
            float maxAngularAcceleration = getValue(values, "maxAngularAcceleration");
            float signatureResolution = getValue(values, "signatureResolution");
            float weaponOptimal = getValue(values, "weaponOptimal");
            float weaponFalloff = getValue(values, "weaponFalloff");
            float weaponDamage = getValue(values, "weaponDamage");
            float weaponCooldown = getValue(values, "weaponCooldown");
            float cost = getTotalCost();

            // TODO: add weapon tracking
            return new ShipStats(mass, inertia, force, force, force, force, maxHealth, maxVelocity,
                    maxAngularVelocity, maxAngularAcceleration, signatureResolution, weaponCooldown,
                    signatureResolution, weaponOptimal, weaponFalloff, weaponDamage, weaponCooldown,
                    maxAcceleration, cost);
        }

        private float getValue(HashMap<String, Float> values, String value) {
            try {
                return values.get(value);
            }
            catch (Throwable t) {
                log(Level.SEVERE, "Couldn't read property " + value + " from the ship definition");
            }
            return Float.NaN;
        }

        public void validate() {
            getStats();
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

        private Window getWindow() {
            return window;
        }
    }

    private class FleetBuild {
        private final Array<ShipBuild> shipBuilds = new Array<ShipBuild>(false, 16);
        private final Window window;
        int activeBuild = -1;
        HorizontalGroup shipSelectionGroup, bottomUIGroup;
        TextButton addButton;
        Label totalCost;
        Table buildTable;
        float oldTotalCost = -1;

        public FleetBuild() {
            window = new Window("Fleet properties", skin);
            window.row().fill().expand();
            shipSelectionGroup = new HorizontalGroup();

            // create a button to add builds
            addButton = new TextButton("+", skin);
            addButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    add(createShipFromCatalog(1));
                }
            });
            shipSelectionGroup.addActor(addButton);
            shipSelectionGroup.pack();
            window.add(shipSelectionGroup);

            buildTable = new Table(skin);
            window.row().fillX();
            window.add(buildTable);

            totalCost = new Label("", skin);
            window.row().left();
            window.add(totalCost);

            bottomUIGroup = new HorizontalGroup();
            TextButton save = new TextButton("Save", skin);
            save.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    saveJson();
                }
            });
            TextButton load = new TextButton("Load", skin);
            load.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    loadJson();
                }
            });
            bottomUIGroup.addActor(save);
            bottomUIGroup.addActor(load);
            TextButton startFight = new TextButton("Test flight", skin);
            startFight.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    startTestFlight();
                }
            });
            bottomUIGroup.addActor(startFight);
            window.row().left();
            window.add(bottomUIGroup);

            window.pack();
        }

        public void loadJson() {
            String rawText = LocalStorage.fetch(LocalStorage.CURRENT_BUILD);

            log(rawText);

            JsonReader reader = new JsonReader();
            JsonValue root = reader.parse(rawText);

            HashMap<String, Integer> indexes = new HashMap<String, Integer>();

            for (JsonValue value : root) {
                indexes.put(value.name(), value.asInt());
            }

            // for now, only load and save the first ship
            ShipBuild build = shipBuilds.first();

            if (build == null) {
                build = createShipFromCatalog(1);
                add(build);
            }

            build.setSliders(indexes);
        }

        public void saveJson() {
            String json = getJson();
            log(json);
            LocalStorage.store(LocalStorage.CURRENT_BUILD, json);
        }

        private String getJson() {
            ShipBuild build = shipBuilds.first();

            if (build == null) {
                return "";
            }

            HashMap<String, Integer> indexes = build.getSliders();
            Json json = new Json();
            StringWriter writer = new StringWriter();
            json.setWriter(writer);
            json.writeObjectStart();

            for (Map.Entry<String, Integer> entry : indexes.entrySet()) {
                json.writeValue(entry.getKey(), entry.getValue());
            }
            json.writeObjectEnd();

            return writer.toString();
        }

        public float getTotalCost() {
            float totalCost = 0;
            for(ShipBuild build : shipBuilds) {
                totalCost += build.getTotalCost();
            }

            return totalCost;
        }

        public void add(ShipBuild shipBuild) {
            // add a button to activate the new build
            final int index = shipBuilds.size;
            TextButton activate = new TextButton(Integer.toString(index + 1), skin);
            activate.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    showOnly(index);
                }
            });
            shipSelectionGroup.addActorBefore(addButton, activate);


            shipBuilds.add(shipBuild);
            showOnly(index);
        }

        private Window getWindow() {
            return window;
        }

        public void updateUI() {
            for (ShipBuild shipBuild : shipBuilds) {
                shipBuild.updateUI();
            }
            float total = getTotalCost();
            if (total != oldTotalCost) {
                oldTotalCost = total;
                totalCost.setText("Fleet total: " + getTotalCost());
                window.pack();
            }
        }

        // shows the one with given index, hides everyone else
        // TODO: maybe use Stack instead of adding and removing?
        public void showOnly(int toBeShown) {
            if (activeBuild == toBeShown) {
                return;
            }
            if (activeBuild != -1) {
                // remove the old one
                buildTable.removeActor(shipBuilds.get(activeBuild).getWindow());
            }
            Window toBeAdded = shipBuilds.get(toBeShown).getWindow();
            buildTable.add(toBeAdded);
            activeBuild = toBeShown;
            window.pack();
        }

        public Array<ShipStats> getStats() {
            Array<ShipStats> stats = new Array<ShipStats>(false, shipBuilds.size);
            for (ShipBuild build : shipBuilds) {
                stats.add(build.getStats());
            }
            return stats;
        }
    }

    private void startTestFlight() {
        serverConnection.send(new JoinServerMessage("gilead", -1, -1));

        for (ShipStats stats : currentBuild.getStats()) {
            serverConnection.send(new ShipStatsMessage(stats));
        }

        game.setScreen(WarpGame.ScreenType.BATTLE);
    }

    public FleetBuildingScreen(ServerConnection serverConnection, WarpGame warpGame) {
        this.game = warpGame;
        this.serverConnection = serverConnection;
        batch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("data/uiskin.json"));
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        stage = new Stage(screenWidth, screenHeight, true);

        currentBuild = new FleetBuild();

        stage.addActor(currentBuild.getWindow());

        currentBuild.add(createShipFromCatalog(1));

        Window buildWindow = currentBuild.getWindow();
        buildWindow.setPosition((screenWidth - buildWindow.getWidth()) / 2f,
                (screenHeight - buildWindow.getHeight()) / 2f);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

        currentBuild.updateUI();

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

    private ShipBuild createShipFromCatalog(int shipTypeId) {
        JsonReader reader = new JsonReader();
        JsonValue catalog = reader.parse(Gdx.files.internal("data/shipCatalog.json"));

        JsonValue shipTypes = catalog.require("shipTypes");

        for (JsonValue shipType : shipTypes) {
            int id = shipType.getInt("id");
            if (id != shipTypeId) {
                continue;
            }

            ShipBuild build = new ShipBuild(id);
            Window window = build.getWindow();

            String typeName = shipType.getString("typeName");
            window.row().fill().expandX().fillX();
            window.add(new Label("Type: " + typeName, skin));

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
            build.addTotal();
            window.pack();
            build.validate();
            return build;
        }

        return null;
    }
}
