package com.theincgi.lwjglApp;

import com.theincgi.lwjglApp.ui.Window;

public class Launcher {
	public static final String APPLICATION_NAME = "Unnamed Application";
	private static Window mainWindow;
	
	public static void main(String[] args) {
		mainWindow = new Window(720, 440, APPLICATION_NAME, null);
	}

	public static Window getMainWindow() {
		return mainWindow;
	}
}



