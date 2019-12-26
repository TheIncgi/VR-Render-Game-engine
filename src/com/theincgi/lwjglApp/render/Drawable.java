package com.theincgi.lwjglApp.render;


import com.theincgi.lwjglApp.ui.Color;

public interface Drawable {
	
	
	public void draw();
	public void drawAsColor(Color color);
	
	public Location getLocation();
	
	public void onDestroy();
}
