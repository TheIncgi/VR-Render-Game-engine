package com.theincgi.lwjglApp.mvc.models;

import java.io.File;

public class SolidObject extends Object3D implements Colideable {
	Bounds bounds;
	private boolean showBounds = false;

	public SolidObject(File model, Bounds bounds, float x, float y, float z) {
		super(model, x, y, z);
		this.bounds = bounds;
	}

	public SolidObject(File model, Bounds bounds) {
		super(model);
		this.bounds = bounds;
	}

	public SolidObject(String model, Bounds bounds, float x, float y, float z) {
		super(model, x, y, z);
		this.bounds = bounds;
	}

	public SolidObject(String model, Bounds bounds, String shaderName) {
		super(model, shaderName);
		this.bounds = bounds;
	}

	public SolidObject(String model, Bounds bounds) {
		super(model);
		this.bounds = bounds;
	}

	@Override
	public Bounds getBounds() {
		return bounds;
	}

	@Override
	public void setShowBounds(boolean show) {
		this.showBounds = true;
	}

	@Override
	public boolean showBounds() {
		return showBounds();
	}
	@Override
	public boolean allowRaytraceHits() {
		return true;
	}
	@Override
	public void draw() {
		draw();
		if(showBounds() && bounds!=null)
			bounds.draw();
	}
}
