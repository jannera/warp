package com.rasanenj.warp;

import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.tablelayout.Cell;

/**
 * Window with UI for browsing the given actors
 *
 * @author gilead
 */
public class ActorBrowsingWindow {

    final Window window;
    final Cell cell;
    final Label desc;
    private int activeIndex = -1;
    private final Array<ActorWithDesc> actors = new Array<ActorWithDesc>(true, 1);

    public ActorBrowsingWindow(String name, Stage stage) {
        window = new Window(name, Assets.skin);
        window.setPosition(100, 100);
        window.setSize(300, 300);
        window.row().fill().expand();
        HorizontalGroup uiGroup = new HorizontalGroup();
        TextButton previous = new TextButton("p", Assets.skin);
        previous.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                previous();
            }
        });
        uiGroup.addActor(previous);
        desc = new Label("", Assets.skin);
        uiGroup.addActor(desc);
        TextButton next = new TextButton("n", Assets.skin);
        next.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                next();
            }
        });
        uiGroup.addActor(next);
        uiGroup.pack();
        window.add(uiGroup);

        window.row().fill().expand();
        cell = window.add();

        window.pack();

        window.setVisible(false);

        stage.addActor(window);
    }

    public void addActor(String s, Actor actor) {
        actors.add(new ActorWithDesc(actor, s));
        desc.setText(s);
        cell.setWidget(actor);
        activeIndex = actors.size - 1;
    }

    private void next() {
        if (activeIndex < actors.size -1) {
            changeActive(1);
        }
    }

    private void previous() {
        if (activeIndex > 0) {
            changeActive(-1);
        }
    }

    private void changeActive(int change) {
        if (activeIndex == -1) {
            return;
        }
        activeIndex += change;
        ActorWithDesc actorWithDesc = actors.get(activeIndex);
        cell.setWidget(actorWithDesc.actor);
        desc.setText(actorWithDesc.desc);
    }

    public Window getWindow() {
        return window;
    }

    private class ActorWithDesc {
        public final Actor actor;
        public final String desc;

        private ActorWithDesc(Actor actor, String desc) {
            this.actor = actor;
            this.desc = desc;
        }
    }
}
