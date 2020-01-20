package com.theincgi.lwjglApp.mvc.models;

import org.lwjgl.util.vector.Vector3f;

import com.theincgi.lwjglApp.misc.RayCast;
import com.theincgi.lwjglApp.render.Location;

public class InvertedBounds implements Bounds{
	private Bounds bound;
	
	public InvertedBounds(Bounds toInvert) {
		this.bound = toInvert;
	}
	
	@Override
	public void draw() {
		bound.draw();
	}
	@Override
	public Colideable getParent() {
		return bound.getParent();
	}
	@Override
	public void setParent(Colideable parent) {
		bound.setParent(parent);
	}
	@Override
	public boolean intersects(Bounds other) {
		return !bound.intersects(bound);
	}
	@Override
	public boolean isIn(float x, float y, float z) {
		return !bound.isIn(x, y, z);
	}
	@Override
	public boolean isIn(float[] a) {
		return !bound.isIn(a);
	}
	@Override
	public boolean isIn(Location l) {
		return !bound.isIn(l);
	}
	@Override
	public boolean isIn(Vector3f v) {
		return !bound.isIn(v);
	}
	@Override
	public boolean isRaycastPassthru(RayCast ray) {
		return !bound.isRaycastPassthru(ray);
	}
	@Override
	public String toString() {
		return "INVERTED: ["+bound.toString()+"]";
	}
}
