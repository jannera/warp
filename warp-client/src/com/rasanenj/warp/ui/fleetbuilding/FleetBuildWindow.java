package com.rasanenj.warp.ui.fleetbuilding;

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

import java.io.StringWriter;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class FleetBuildWindow {
    private final Array<ShipBuildWindow> shipBuilds = new Array<ShipBuildWindow>(false, 16);
    private final Window window;
    ShipBuildWindow activeBuild = null;
    final HorizontalGroup shipSelectionGroup, topUIGroup, bottomUIGroup;
    Label totalCost;
    Table buildTable;
    private final TextButton startFight;

    public FleetBuildWindow() {
        window = new Window("Fleet properties", Assets.skin);
        window.row().fill().expand();
        topUIGroup = new HorizontalGroup();
        shipSelectionGroup = new HorizontalGroup();
        topUIGroup.addActor(shipSelectionGroup);

        // create a button to add builds
        TextButton  addButton = new TextButton("+", Assets.skin);
        addButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                add(ShipBuildWindow.createShipFromCatalog(1));
            }
        });
        topUIGroup.addActor(addButton);

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
        startFight = new TextButton("Test flight", Assets.skin);
        bottomUIGroup.addActor(startFight);
        window.row().left();
        window.add(bottomUIGroup);

        window.pack();
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
        log("updated fleet UI");
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

    public Array<ShipStats> getStats() {
        Array<ShipStats> stats = new Array<ShipStats>(false, shipBuilds.size);
        for (ShipBuildWindow build : shipBuilds) {
            stats.add(build.getStats());
        }
        return stats;
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