package com.theincgi.lwjglApp.mvc.models;

import com.theincgi.lwjglApp.render.Drawable;

public interface Colideable extends Drawable{
	/**Returns the bounding box of this object if it exists*/
	public Bounds getBounds();
	public void setShowBounds(boolean show);
	public boolean showBounds();
	
}
