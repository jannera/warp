package com.rasanenj.warp.ui.fleetbuilding;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.rasanenj.warp.Assets;
import com.rasanenj.warp.entities.ShipStats;
import com.rasanenj.warp.storage.LocalStorage;
import com.rasanenj.warp.ui.PropertySlider;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class FleetBuildWindow {
    private final Array<ShipBuildWindow> shipBuilds = new Array<ShipBuildWindow>(false, 16);
    private final Window window;
    int activeBuild = -1;
    HorizontalGroup shipSelectionGroup, bottomUIGroup;
    TextButton addButton;
    Label totalCost;
    Table buildTable;
    float oldTotalCost = -1;
    private final TextButton startFight;

    public FleetBuildWindow() {
        window = new Window("Fleet properties", Assets.skin);
        window.row().fill().expand();
        shipSelectionGroup = new HorizontalGroup();

        // create a button to add builds
        addButton = new TextButton("+", Assets.skin);
        addButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                add(createShipFromCatalog(1));
            }
        });
        shipSelectionGroup.addActor(addButton);
        shipSelectionGroup.pack();
        window.add(shipSelectionGroup);

        buildTable = new Table(Assets.skin);
        window.row().fillX();
        window.add(buildTable);

        totalCost = new Label("", Assets.skin);
        window.row().left();
        window.add(totalCost);

        bottomUIGroup = new HorizontalGroup();
        TextButton save = new TextButton("Save", Assets.skin);
        save.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                saveCurrentBuild();
            }
        });
        TextButton load = new TextButton("Load", Assets.skin);
        load.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                loadCurrentBuild();
            }
        });
        bottomUIGroup.addActor(save);
        bottomUIGroup.addActor(load);
        startFight = new TextButton("Test flight", Assets.skin);
        bottomUIGroup.addActor(startFight);
        window.row().left();
        window.add(bottomUIGroup);

        window.pack();
    }

    private static final String TYPE_ID = "typeId", INDEXES = "indexes";

    public void loadFromJson(String rawText) {
        JsonReader reader = new JsonReader();
        JsonValue root = reader.parse(rawText);

        clear();

        for (JsonValue jShipBuild : root) {
            int typeid = jShipBuild.require(TYPE_ID).asInt();
            JsonValue jIndexes = jShipBuild.require(INDEXES);
            HashMap<String, Integer> indexes = new HashMap<String, Integer>();

            for (JsonValue value : jIndexes) {
                indexes.put(value.name(), value.asInt());
            }

            ShipBuildWindow build = createShipFromCatalog(typeid);
            build.setSliders(indexes);
            add(build);
        }
    }

    private void clear() {
        if (activeBuild != -1) {
            buildTable.removeActor(shipBuilds.get(activeBuild).getWindow());
        }
        shipBuilds.clear();
        shipSelectionGroup.clear();
        shipSelectionGroup.addActor(addButton);
        shipSelectionGroup.pack();
        activeBuild = -1;
        oldTotalCost = -1;
    }

    public String getJson() {
        Json json = new Json();
        StringWriter writer = new StringWriter();
        json.setWriter(writer);

        json.writeArrayStart();
        for (ShipBuildWindow build : shipBuilds) {
            HashMap<String, Integer> indexes = build.getSliders();
            json.writeObjectStart();

            json.writeValue(TYPE_ID, build.getTypeId());

            json.writeObjectStart(INDEXES);

            for (Map.Entry<String, Integer> entry : indexes.entrySet()) {
                json.writeValue(entry.getKey(), entry.getValue());
            }
            json.writeObjectEnd();
            json.writeObjectEnd();
        }
        json.writeArrayEnd();

        return writer.toString();
    }

    public float getTotalCost() {
        float totalCost = 0;
        for(ShipBuildWindow build : shipBuilds) {
            totalCost += build.getTotalCost();
        }

        return totalCost;
    }

    public void add(ShipBuildWindow shipBuild) {
        // add a button to activate the new build
        final int index = shipBuilds.size;
        TextButton activate = new TextButton(Integer.toString(index + 1), Assets.skin);
        activate.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showOnly(index);
            }
        });
        shipBuild.setActivateButton(activate);
        shipSelectionGroup.addActorBefore(addButton, activate);

        shipBuilds.add(shipBuild);
        showOnly(index);
    }

    public Window getWindow() {
        return window;
    }

    public void updateUI() {
        for (ShipBuildWindow shipBuild : shipBuilds) {
            shipBuild.updateUI();
        }
        float total = getTotalCost();
        if (total != oldTotalCost) {
            // TODO: this is not a good mechanism, as we need to reset the oldTotalCost
            // when clearing the screen. better mechanism would be if updateUI would return
            // true when their state has changed.. ?
            // .. or just listen to changes, and only then do changes to labels..
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
            ShipBuildWindow toBeRemoved = shipBuilds.get(activeBuild);
            toBeRemoved.getActivateButton().setColor(Color.WHITE);
            buildTable.removeActor(toBeRemoved.getWindow());
        }
        shipBuilds.get(toBeShown).getActivateButton().setColor(Color.GRAY);
        Window toBeAdded = shipBuilds.get(toBeShown).getWindow();
        buildTable.add(toBeAdded);
        activeBuild = toBeShown;
        window.pack();
    }

    public Array<ShipStats> getStats() {
        Array<ShipStats> stats = new Array<ShipStats>(false, shipBuilds.size);
        for (ShipBuildWindow build : shipBuilds) {
            stats.add(build.getStats());
        }
        return stats;
    }

    public static ShipBuildWindow createShipFromCatalog(int shipTypeId) {
        JsonReader reader = new JsonReader();
        JsonValue catalog = reader.parse(Gdx.files.internal("data/shipCatalog.json"));

        JsonValue shipTypes = catalog.require("shipTypes");

        for (JsonValue shipType : shipTypes) {
            int id = shipType.getInt("id");
            if (id != shipTypeId) {
                continue;
            }

            ShipBuildWindow build = new ShipBuildWindow(id);
            Window window = build.getWindow();

            String typeName = shipType.getString("typeName");
            window.row().fill().expandX().fillX();
            window.add(new Label("Type: " + typeName, Assets.skin));

            JsonValue categories = shipType.require("categories");
            for (JsonValue category : categories) {
                window.row().fill().expandX();
                window.add(new Label(category.getString("name"), Assets.skin));

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

    public void loadCurrentBuild() {
        String rawText = LocalStorage.fetch(LocalStorage.CURRENT_BUILD);
        log(rawText);
        loadFromJson(rawText);
    }

    private void saveCurrentBuild() {
        String json = getJson();
        log(json);
        LocalStorage.store(LocalStorage.CURRENT_BUILD, json);
    }

    public TextButton getStartFight() {
        return startFight;
    }
}