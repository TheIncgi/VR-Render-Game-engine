package com.theincgi.lwjglApp.mvc.models;

import org.lwjgl.util.vector.Vector3f;

import com.theincgi.lwjglApp.misc.RayCast;
import com.theincgi.lwjglApp.render.Location;

public class NoBound implements Bounds{
	
	private Colideable parent;
	public NoBound() {
	}
	
	@Override
	public boolean intersects(Bounds other) {
		return false;
	}
	@Override
	public boolean isIn(float x, float y, float z) {
		return false;
	}
	@Override
	public boolean isIn(float[] a) {
		return false;
	}
	
	@Override
	public boolean isIn(Location l) {
		return false;
	}
	@Override
	public boolean isIn(Vector3f v) {
		return false;
	}
	
	@Override
	public boolean isRaycastPassthru(RayCast ray) {
		return false;
	}
	
	@Override
	public void setParent(Colideable parent) {
		this.parent = parent;
	}
	
	@Override
	public Colideable getParent() {
		return parent;
	}
	@Override
	public void draw() {	
	}
	
}
