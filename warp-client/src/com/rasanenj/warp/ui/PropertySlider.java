package com.rasanenj.warp.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.rasanenj.warp.Assets;

/**
 * @author gilead
 */
public class PropertySlider {
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
        Label label = new Label(propertyName, Assets.skin);
        window.row().fill().expandX().fillX();
        window.add(label).padLeft(10);
        slider = new Slider(0, costs.length - 1, 1, false, Assets.skin);
        window.add(slider);
        description = new Label("", Assets.skin);
        update();
        window.add(description);
    }

    public String getId() {
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