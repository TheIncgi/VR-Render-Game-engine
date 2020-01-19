package com.theincgi.lwjglApp.mvc.models;

import java.io.File;

public class SolidObject extends Object3D implements Colideable {
	Bounds bounds;

	public SolidObject(File model, Bounds bounds, float x, float y, float z) {
		super(model, x, y, z);
	}

	public SolidObject(File model, Bounds bounds) {
		super(model);
	}

	public SolidObject(String model, Bounds bounds, float x, float y, float z) {
		super(model, x, y, z);
	}

	public SolidObject(String model, Bounds bounds, String shaderName) {
		super(model, shaderName);
	}

	public SolidObject(String model, Bounds bounds) {
		super(model);
	}
	
}
