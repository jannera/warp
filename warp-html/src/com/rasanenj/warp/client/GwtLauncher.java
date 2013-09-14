package com.rasanenj.warp.client;

import com.google.gwt.user.client.Window;
import com.rasanenj.warp.WarpGame;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;

public class GwtLauncher extends GwtApplication {
	@Override
	public GwtApplicationConfiguration getConfig () {
        int width = WarpGame.scaleSize(Window.getClientWidth());
        int height = WarpGame.scaleSize(Window.getClientHeight());
		GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(width, height);
		return cfg;
	}

	@Override
	public ApplicationListener getApplicationListener () {
		return new WarpGame();
	}
}