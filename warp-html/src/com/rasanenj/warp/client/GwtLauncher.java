package com.rasanenj.warp.client;

import com.rasanenj.warp.WarpGame;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;

public class GwtLauncher extends GwtApplication {
	@Override
	public GwtApplicationConfiguration getConfig () {
		GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(800, 600);
		return cfg;
	}

	@Override
	public ApplicationListener getApplicationListener () {
		return new WarpGame();
	}
}