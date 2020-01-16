package com.theincgi.lwjglApp.misc;

import org.lwjgl.util.vector.Vector4f;

import com.theincgi.lwjglApp.render.Drawable;

public class RayCast {
	public Vector4f worldOffset, rayDirection;
	public Vector4f result = null;
	public Drawable raycastedObject;
	
	public RayCast(Vector4f worldOffset, Vector4f rayDirection) {
		this.worldOffset = worldOffset;
		this.rayDirection = rayDirection;
	}
}
