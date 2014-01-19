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
    int activeBuild = -1;
    HorizontalGroup shipSelectionGroup, bottomUIGroup;
    TextButton addButton;
    Label totalCost;
    Table buildTable;
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
                add(ShipBuildWindow.createShipFromCatalog(1));
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
        if (activeBuild != -1) {
            buildTable.removeActor(shipBuilds.get(activeBuild).getWindow());
        }
        shipBuilds.clear();
        shipSelectionGroup.clear();
        shipSelectionGroup.addActor(addButton);
        shipSelectionGroup.pack();
        activeBuild = -1;
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

    public void add(ShipBuildWindow shipBuild) {
        // add a button to activate the new build
        final int index = shipBuilds.size;
        TextButton activate = shipBuild.getActivateButton();
        activate.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showOnly(index);
            }
        });
        shipSelectionGroup.addActorBefore(addButton, activate);

        shipBuild.getWindow().addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateUI();
            }
        });

        shipBuilds.add(shipBuild);
        showOnly(index);

        updateUI();
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