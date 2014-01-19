package com.rasanenj.warp.ui.fleetbuilding;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.rasanenj.warp.Assets;
import com.rasanenj.warp.entities.ShipStats;
import com.rasanenj.warp.ui.PropertySlider;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class ShipBuildWindow {
    private final int typeId;
    private final Window window;
    private Label total, amountLabel;
    private TextButton activateButton, plusAmount, minusAmount;
    private int amount = 1;
    private final String icon;

    public ShipBuildWindow(int typeId, String icon) {
        this.icon = icon;
        window = new Window("Ship properties", Assets.skin);
        window.setMovable(false);
        this.typeId = typeId;

        window.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateUI();
            }
        });
    }

    public void updateUI() {
        log("updated shipbuild UI");
        total.setText("Total: " + getTotalCost());
        window.pack();
    }

    private void add(PropertySlider slider) {
        sliders.add(slider);

    }

    public void createActivateButton() {
        activateButton = new TextButton("", Assets.skin);
    }

    private void createTotalLabel() {
        total = new Label("", Assets.skin);
        window.row().fill().expand();
        window.add(total).expand().fill().colspan(0);
    }

    private void createAmountUI() {
        window.row().fill().expand();
        window.add(new Label("Amount", Assets.skin));
        HorizontalGroup group = new HorizontalGroup();
        amountLabel = new Label("", Assets.skin);
        plusAmount = new TextButton("+", Assets.skin);
        minusAmount = new TextButton("-", Assets.skin);

        plusAmount.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setAmount(amount + 1);
            }
        });

        minusAmount.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setAmount(amount - 1);
            }
        });

        group.addActor(amountLabel);
        group.addActor(plusAmount);
        group.addActor(minusAmount);
        window.add(group);

        setAmount(1);
    }

    private void setAmount(int amount) {
        if (amount < 1) {
            return;
        }
        this.amount = amount;
        String amountText = Integer.toString(this.amount);
        amountLabel.setText(amountText + " ");
        activateButton.setText(amountText + " x " + icon);
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
        return totalCost * amount;
    }

    private final Array<PropertySlider> sliders = new Array<PropertySlider>(false, 16);

    public Window getWindow() {
        return window;
    }

    public int getTypeId() {
        return typeId;
    }

    public TextButton getActivateButton() {
        return activateButton;
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

            String icon = shipType.getString("icon");

            ShipBuildWindow build = new ShipBuildWindow(id, icon);
            Window window = build.getWindow();
            build.createActivateButton();

            String typeName = shipType.getString("typeName");
            window.row().fill().expandX().fillX();
            window.add(new Label("Type", Assets.skin));
            window.add(new Label(typeName, Assets.skin));

            build.createAmountUI();

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
            build.createTotalLabel();
            build.updateUI();
            build.validate();
            return build;
        }

        return null;
    }

    private static final String TYPE_ID = "typeId", INDEXES = "indexes", AMOUNT = "amount";

    public void writeToJson(Json json) {
        HashMap<String, Integer> indexes = getSliders();
        json.writeObjectStart();

        json.writeValue(TYPE_ID, getTypeId());

        json.writeValue(AMOUNT, amount);

        json.writeObjectStart(INDEXES);

        for (Map.Entry<String, Integer> entry : indexes.entrySet()) {
            json.writeValue(entry.getKey(), entry.getValue());
        }
        json.writeObjectEnd();
        json.writeObjectEnd();
    }

    public static ShipBuildWindow loadFromJson(JsonValue jShipBuild) {
        int typeid = jShipBuild.require(TYPE_ID).asInt();
        int amount = jShipBuild.require(AMOUNT).asInt();
        JsonValue jIndexes = jShipBuild.require(INDEXES);
        HashMap<String, Integer> indexes = new HashMap<String, Integer>();

        for (JsonValue value : jIndexes) {
            indexes.put(value.name(), value.asInt());
        }

        ShipBuildWindow build = ShipBuildWindow.createShipFromCatalog(typeid);
        build.setSliders(indexes);
        build.setAmount(amount);
        return build;
    }
}