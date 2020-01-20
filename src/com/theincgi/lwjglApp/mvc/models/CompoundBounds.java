package com.theincgi.lwjglApp.mvc.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import org.lwjgl.util.vector.Vector3f;

import com.theincgi.lwjglApp.misc.RayCast;
import com.theincgi.lwjglApp.render.Location;

/**Effectively a union of bounds*/
public class CompoundBounds implements Bounds {
	private final ArrayList<Bounds> bounds = new ArrayList<>();
	private Colideable parent;
	public CompoundBounds(Bounds...bounds) {
		Collections.addAll(this.bounds, bounds);
	}

	@Override
	public boolean isIn(float x, float y, float z) {
		for (Bounds b : bounds) {
			if(b.isIn(x, y, z)) return true;
		}return false;
	}

	@Override
	public boolean isIn(float[] a) {
		for (Bounds b : bounds) {
			if(b.isIn(a)) return true;
		}return false;
	}

	@Override
	public boolean isIn(Vector3f v) {
		for (Bounds b : bounds) {
			if(b.isIn(v)) return true;
		}return false;
	}

	@Override
	public boolean isIn(Location l) {
		for (Bounds b : bounds) {
			if(b.isIn(l)) return true;
		}return false;
	}

	@Override
	public boolean intersects(Bounds other) {
		for (Bounds b : bounds) {
			if(b.intersects(other)) return true;
		}return false;
	}

	@Override
	public boolean isRaycastPassthru(RayCast ray) {
		for (Bounds b : bounds) {
			if(b.isRaycastPassthru(ray)) return true;
		}return false;
	}
	
	public ArrayList<Bounds> getBoundsList() {
		return bounds;
	}

	public void addBounds(Bounds b) {
		bounds.add(b);
	}
	
	@Override
	public Colideable getParent() {
		return parent;
	}
	/**Sets teh parent of this and child elements*/
	@Override
	public void setParent(Colideable parent) {
		this.parent = parent;
		for (Bounds b : bounds)
			b.setParent(parent);
	}
	/**Does nothing, should be handled per bound so parent classes do not render bounds of the child elements twice*/
	@Override
	public void draw() {
	}
	
}
