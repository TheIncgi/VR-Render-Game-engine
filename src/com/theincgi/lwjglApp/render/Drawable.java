package com.theincgi.lwjglApp.render;


import com.theincgi.lwjglApp.ui.Color;

public interface Drawable {
	
	
	public void draw(float[] mvpm);
	public void drawAsColor(float[] mvpm, Color color);
	
	public Location getLocation();
	
	public void onDestroy();
}
