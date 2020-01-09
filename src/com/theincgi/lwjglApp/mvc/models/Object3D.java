package com.theincgi.lwjglApp.mvc.models;

import java.io.File;
import java.util.Optional;

import com.theincgi.lwjglApp.render.Drawable;
import com.theincgi.lwjglApp.render.Location;
import com.theincgi.lwjglApp.render.Model;
import com.theincgi.lwjglApp.render.ObjManager;
import com.theincgi.lwjglApp.render.shaders.ShaderManager;

public class Object3D implements Drawable{
	Optional<Model> model;
	Location location;
	
	public Object3D(File model) {
		this(model, 0,0,0);
	}
	public Object3D(String model) {
		this(new File(model));
	}
	public Object3D(String model, float x, float y, float z) {
		this(new File(model),x,y,z);
	}
	public Object3D(File model, float x, float y, float z) {
		this.model = ObjManager.INSTANCE.get(model, "full");
		this.location = new Location(x, y, z);
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void draw() {
		model.ifPresent(m->m.drawAt(location));
	}
	public void setShader(String shaderName) {
		model.ifPresent(m->m.shader=ShaderManager.INSTANCE.get(shaderName));
	}
}
