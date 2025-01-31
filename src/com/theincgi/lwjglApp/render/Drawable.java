package com.theincgi.lwjglApp.render;

public interface Drawable {
	public void draw();
	/**A drawable must return true if it can at any point render something transparent<br>
	 * for transparent objects to render correctly the should be sorted by distance for rendering<br>
	 * if an object will never be transparent, set this to false so there are fewer objects to sort in the scene*/
	public boolean isTransparent();
	/**Used to sort transparent objects<br>
	 * Opaque objects are not required to return a value for this*/
	public float[] getTransparentObjectPos();
}
