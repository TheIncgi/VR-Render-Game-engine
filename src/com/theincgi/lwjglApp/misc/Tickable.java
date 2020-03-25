package com.theincgi.lwjglApp.misc;

@FunctionalInterface
public interface Tickable {
	/**Returns true if the object no longer needs to be updated*/
	public boolean onTickUpdate();//TODO add an argument specifiying the amount of time passed since the last update
}
