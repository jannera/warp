package com.rasanenj.warp.ui.fleetbuilding;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Array;
import com.rasanenj.warp.Assets;
import com.rasanenj.warp.entities.ShipStats;
import com.rasanenj.warp.ui.PropertySlider;

import java.util.HashMap;
import java.util.logging.Level;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class ShipBuildWindow {
    private final int typeId;
    private final Window window;
    private Label total;

    public ShipBuildWindow(int typeId) {
        window = new Window("Ship properties", Assets.skin);
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
        total = new Label("", Assets.skin);
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

    public Window getWindow() {
        return window;
    }

    public int getTypeId() {
        return typeId;
    }
}