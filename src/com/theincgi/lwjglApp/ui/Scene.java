package com.theincgi.lwjglApp.ui;


import java.util.Optional;

public class Scene {
	public Color clearColor = Color.SKY.clone();
	protected Optional<CallbackListener> sceneListener = Optional.empty(); 
	
	
	public void render(double mouseX, double mouseY) {}
	
	public Optional<CallbackListener> getSceneListener() {
		return sceneListener;
	}
	
	public void onUnload(){}
}
