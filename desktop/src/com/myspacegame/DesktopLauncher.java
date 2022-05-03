package com.myspacegame;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
//		config.setIdleFPS(5); // this breaks camera.unproject for mouse world pos
//		config.setMaximized(true);
		config.setWindowedMode(800, 600);
		config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 3);
		new Lwjgl3Application(new MainClass(), config);
	}
}
