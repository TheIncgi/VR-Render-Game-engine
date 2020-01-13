package com.theincgi.lwjglApp.mvc.models.particle;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import com.theincgi.lwjglApp.ui.Color;

public class Particle {
	public long expiration;
	public long birth;
	public Vector3f position;
	public Vector3f velocity;
	public float energy;
	public Color color;
	public boolean justBorn = true;
	
	public Particle(long expiration, long birth, Vector3f position, Vector3f velocity, float energy, Color color) {
		this.expiration = expiration;
		this.birth = birth;
		this.position = position;
		this.velocity = velocity;
		this.energy = energy;
		this.color = color;
	}
	
	public Particle() {
		position = new Vector3f();
		velocity = new Vector3f();
	}

	public void copyFrom(Particle p) {
		this.expiration = p.expiration;
		this.birth = p.birth;
		this.position.set(p.position);
		this.velocity.set(p.velocity);
		this.energy = p.energy;
		this.color = p.color.clone();
		this.justBorn = p.justBorn;
	}
	
	
}
