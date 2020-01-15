package com.theincgi.lwjglApp.mvc.models;

import org.lwjgl.util.vector.Vector3f;

import com.theincgi.lwjglApp.misc.RayCast;
import com.theincgi.lwjglApp.render.Location;

public interface Bounds {

	boolean isIn(float x, float y, float z);

	boolean isIn(float[] a);

	boolean isIn(Vector3f v);

	boolean isIn(Location l);

	boolean intersects(Bounds other);
	
	boolean isRaycastPassthru(RayCast ray);
}
