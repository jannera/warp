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
import com.rasanenj.warp.messaging.ServerConnection;
import com.rasanenj.warp.messaging.ShipStatsMessage;
import com.rasanenj.warp.storage.LocalStorage;

import java.io.StringWriter;
import java.util.HashMap;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class FleetBuildWindow {
    private final Array<ShipBuildWindow> shipBuilds = new Array<ShipBuildWindow>(false, 16);
    private final Window window;
    ShipBuildWindow activeBuild = null;
    final HorizontalGroup shipSelectionGroup, topUIGroup, bottomUIGroup;
    final SelectBox shipTypeSelect;
    Label totalCost;
    Table buildTable;
    private final TextButton startFight;
    private final HashMap<String, Integer> shipTypes;

    public FleetBuildWindow() {
        shipTypes = readShipTypesFromJSON();

        window = new Window("Fleet properties", Assets.skin);
        window.row().fill().expand();
        topUIGroup = new HorizontalGroup();
        shipSelectionGroup = new HorizontalGroup();
        topUIGroup.addActor(shipSelectionGroup);

        // drop down menu for selecting new ship type
        shipTypeSelect = createShipSelector();
        topUIGroup.addActor(shipTypeSelect);

        // button to add builds
        TextButton  addButton = new TextButton("+", Assets.skin);
        addButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int type = getCurrentlySelectedShipType();
                add(ShipBuildWindow.createShipFromCatalog(type));
            }
        });
        topUIGroup.addActor(addButton);

        // button to remove builds
        TextButton removeButton = new TextButton("-", Assets.skin);
        removeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                removeCurrentBuild();
            }
        });
        topUIGroup.addActor(removeButton);

        topUIGroup.pack();
        window.add(topUIGroup);

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
        startFight = new TextButton("Deploy", Assets.skin);
        bottomUIGroup.addActor(startFight);
        window.row().left();
        window.add(bottomUIGroup);

        window.pack();
    }

    private SelectBox createShipSelector() {
        Object[] shipTypeNames = new Object[shipTypes.size()];
        int i = 0;
        for (String name : shipTypes.keySet()) {
            shipTypeNames[i++] = name;
        }
        return new SelectBox(shipTypeNames, Assets.skin);
    }

    private HashMap<String, Integer> readShipTypesFromJSON() {
        HashMap<String, Integer> result = new HashMap<String, Integer>(2);

        JsonReader reader = new JsonReader();
        JsonValue catalog = reader.parse(Gdx.files.internal("data/shipCatalog.json"));

        JsonValue shipTypes = catalog.require("shipTypes");

        for (JsonValue shipType : shipTypes) {
            int id = shipType.getInt("id");
            String typeName = shipType.getString("typeName");

            result.put(typeName, id);
        }

        return result;
    }

    private int getCurrentlySelectedShipType() {
        return shipTypes.get(shipTypeSelect.getSelection());
    }

    private void removeCurrentBuild() {
        if (shipBuilds.size <= 1) {
            return;
        }
        if (activeBuild == null) {
            return;
        }
        ShipBuildWindow toBeRemoved = activeBuild;

        int index = shipBuilds.indexOf(toBeRemoved, true);
        int nextIndex = index + 1;
        if (nextIndex >= shipBuilds.size) {
            nextIndex = index -1;
        }
        ShipBuildWindow nextToBeActive = shipBuilds.get(nextIndex);
        showOnly(nextToBeActive);

        shipBuilds.removeValue(toBeRemoved, true);
        shipSelectionGroup.removeActor(toBeRemoved.getActivateButton());

        shipSelectionGroup.pack();
        updateUI();
        saveCurrentBuild();
    }

    public void loadFromJson(String rawText) {
        JsonReader reader = new JsonReader();
        JsonValue root = reader.parse(rawText);

        clear();

        for (JsonValue jShipBuild : root) {
            ShipBuildWindow build = ShipBuildWindow.loadFromJson(jShipBuild);
            add(build);
        }
    }

    private void clear() {
        if (activeBuild != null) {
            buildTable.removeActor(activeBuild.getWindow());
        }
        shipBuilds.clear();
        shipSelectionGroup.clear();
        shipSelectionGroup.pack();
        topUIGroup.pack();
        activeBuild = null;
    }

    public String getJson() {
        Json json = new Json();
        StringWriter writer = new StringWriter();
        json.setWriter(writer);

        json.writeArrayStart();
        for (ShipBuildWindow build : shipBuilds) {
            build.writeToJson(json);
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

    public void add(final ShipBuildWindow shipBuild) {
        // add a button to activate the new build
        TextButton activate = shipBuild.getActivateButton();
        activate.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showOnly(shipBuild);
            }
        });
        shipSelectionGroup.addActor(activate);
        shipSelectionGroup.pack();

        shipBuild.getWindow().addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateUI();
                saveCurrentBuild();
            }
        });

        shipBuilds.add(shipBuild);
        showOnly(shipBuild);

        updateUI();
        saveCurrentBuild();
    }

    public Window getWindow() {
        return window;
    }

    public void updateUI() {
        totalCost.setText("Fleet total: " + getTotalCost());
        window.pack();
    }

    // shows the one with given index, hides everyone else
    public void showOnly(ShipBuildWindow toBeShown) {
        if (activeBuild != null) {
            // remove the old one
            activeBuild.getActivateButton().setColor(Color.WHITE);
            buildTable.removeActor(activeBuild.getWindow());
        }
        toBeShown.getActivateButton().setColor(Color.GRAY);
        Window toBeAdded = toBeShown.getWindow();
        buildTable.add(toBeAdded);
        activeBuild = toBeShown;
        window.pack();
    }

    public void loadCurrentBuild() {
        String rawText = LocalStorage.fetch(LocalStorage.CURRENT_BUILD);
        if (rawText == null) {
            return;
        }
        loadFromJson(rawText);
    }

    private void saveCurrentBuild() {
        String json = getJson();
        LocalStorage.store(LocalStorage.CURRENT_BUILD, json);
    }

    public TextButton getStartFight() {
        return startFight;
    }

    public void deploy(ServerConnection conn, long ownerId, float x, float y) {
        // todo: do not create one message per ship type
        // todo: instead add amount to message

        for (ShipBuildWindow build : shipBuilds) {
            conn.send(new ShipStatsMessage(build.getStats(), ownerId, x, y, build.getAmount()));
        }
    }
}