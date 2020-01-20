package com.theincgi.lwjglApp.mvc.models;

import java.io.File;


public class SolidObject extends Object3D implements Colideable {
	Bounds bounds;
	private boolean showBounds = false;

	public SolidObject(File model, Bounds bounds, float x, float y, float z) {
		super(model, x, y, z);
		this.bounds = bounds;
		this.bounds.setParent(this);
	}

	public SolidObject(File model, Bounds bounds) {
		super(model);
		this.bounds = bounds;
		this.bounds.setParent(this);
	}

	public SolidObject(String model, Bounds bounds,  float x, float y, float z) {
		super(model, x, y, z);
		this.bounds = bounds;
		this.bounds.setParent(this);
	}

	public SolidObject(String model, Bounds bounds,  String shaderName) {
		super(model, shaderName);
		this.bounds = bounds;
		this.bounds.setParent(this);
	}

	public SolidObject(String model, Bounds bounds) {
		super(model);
		this.bounds = bounds;
		this.bounds.setParent(this);
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
		return showBounds;
	}
	@Override
	public boolean allowRaytraceHits() {
		return true;
	}
	
	public SolidObject setBounds(Bounds bounds) {
		this.bounds = bounds;
		return this;
	}
	
	@Override
	public void draw() {
		super.draw();
		if(showBounds() && bounds!=null)
			bounds.draw();
	}
}
