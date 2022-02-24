package com.myspacegame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.myspacegame.screens.LoadingScreen;
import com.myspacegame.screens.MainMenuScreen;
import com.myspacegame.ui.UIStyles;

public class MainClass extends Game {

	public AssetManager assetManager;
	public UIStyles uiStyles;

	@Override
	public void create () {
		assetManager = new AssetManager();
		Info.init();

		setScreen(new LoadingScreen(this));

	}

}
