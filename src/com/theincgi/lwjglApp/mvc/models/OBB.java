package com.theincgi.lwjglApp.mvc.models;

import org.lwjgl.util.vector.Vector3f;

import com.theincgi.lwjglApp.render.Location;

public class OBB implements Bounds{
	Vector3f
		origin,
		right,
		forward,
		up;

	public OBB(Vector3f origin, Vector3f right, Vector3f forward, Vector3f up) {
		super();
		this.origin = origin;
		this.right = right;
		this.forward = forward;
		this.up = up;
	}
	
	//expected rules of collision:
	//1. An OBB will have at least 1 line pass through 1 face of the other OBB (may have to flip which ones faces vs which ones lines are being tested)
	//2. An OBB can still be colliding if it is entirely contained by another OBB, in which case a corner must be inside of the other OBB.
	
	//expected rules of containment:
	//1. a point projected on to 3 different (non parallel) faces should exist within that face (not be on the plane extended off of that face)
	//2. a point should have a negative normal to get to it, this requires 6 tests however...
	//   
	@Override
	public boolean isIn(float x, float y, float z) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean isIn(float[] a) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean isIn(Vector3f v) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean isIn(Location l) {
		return isIn(l.getX(), l.getY(), l.getZ());
	}
	@Override
	public boolean intersects(Bounds other) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
}
