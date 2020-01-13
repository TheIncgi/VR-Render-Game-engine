package com.theincgi.lwjglApp.misc;

public interface Tickable {
	/**Returns true if the object no longer needs to be updated*/
	public boolean onTickUpdate();
}
