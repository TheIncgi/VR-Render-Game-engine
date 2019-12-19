package com.theincgi.lwjglApp.mvc.view.drawables;

import java.util.Optional;

import com.theincgi.lwjglApp.mvc.view.shaders.ShaderProgram;
import com.theincgi.lwjglApp.ui.Color;

public interface Drawable {
	Optional<ShaderProgram> shader = Optional.empty();
	
	public void draw(float[] mvpm);
	public void drawAsColor(float[] mvpm, Color color);
	
	public void onDestroy();
}
