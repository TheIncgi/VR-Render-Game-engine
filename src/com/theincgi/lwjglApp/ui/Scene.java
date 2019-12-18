package com.theincgi.lwjglApp.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Scene {
	Color clearColor = Color.BLACK.clone();
	protected Optional<CallbackListener> sceneListener = Optional.empty(); 
	
	
	public void render(double mouseX, double mouseY) {}
	
	public Optional<CallbackListener> getSceneListener() {
		return sceneListener;
	}
}
