package com.theincgi.lwjglApp.render;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Optional;

import org.lwjgl.util.vector.Vector3f;

import com.theincgi.lwjglApp.misc.Tickable;
import com.theincgi.lwjglApp.render.ParticleSystem.Particle;
import com.theincgi.lwjglApp.ui.Color;
import com.theincgi.lwjglApp.ui.Scene;

public class ParticleSystem implements Drawable, Tickable{
	Optional<Model> particle = Optional.empty();
	Location source;
	private Scene scene;
	/**The number of particles to create during this cycle*/
	private int spawnAmount;
	/**The number of particles already created*/
	private int spawnCount;
	/**How long it will take to emit all particles*/
	private long timeSpan;
	private long startTime;
	
	private LinkedList<Particle> theParticles = new LinkedList<>();
	private ArrayList<Emitter> emitters = new ArrayList<>();
	private ArrayList<Collector> collectors = new ArrayList<>();
	private ArrayList<Force> forces = new ArrayList<>();
	private long maxAge;
	private float ageNoise;
	
	/**Registers self to drawable and tickable listeners when `emit()` is called, removes self when no particles remain*/
	public ParticleSystem(Scene scene, float x, float y, float z) {
		this.scene = scene;
		this.source = new Location(x,y,z);
		if(particle.isEmpty())
			particle = ObjManager.INSTANCE.get("cmodels/softcube/softcube.obj", "full");
		
	}
	
	/**
	 * Emits the number of particles over some number of milliseconds<br>
	 * this will stop the spawning of the previous group
	 * @param amount number of particles to create
	 * @param timeSpan how long will it take to emit all particles from this group
	 * */
	public void emit(int amount, long timeSpan, long maxAge, float ageNoise) {
		this.spawnAmount = amount;
		this.timeSpan = timeSpan;
		spawnCount = 0;
		this.maxAge = maxAge;
		this.ageNoise = ageNoise;
		startTime = System.currentTimeMillis();
	}
	
	
	public void addEmitter(Emitter e) {
		
	}
	
	@Override
	public void draw() {
		particle.ifPresent(p->p.drawAt(source));
	}
	
	@Override
	public boolean onTickUpdate() {
		long now = System.currentTimeMillis();
		int target = (int) Math.min(spawnCount, (now-startTime)* spawnAmount / timeSpan);
		Particle temp = new Particle();
		while(spawnCount < target) {
			addParticle();
			spawnCount++;
		}
		synchronized (theParticles) {
			ListIterator<Particle> pit = theParticles.listIterator();
			while(pit.hasNext()) {
				Particle p = pit.next();
				synchronized (collectors) {
					for (Collector collector : collectors) {
						if (collector.shouldCollect(p)) {
							pit.remove();
							continue;
						}
					}
				}
				synchronized (forces) {
					temp.copyFrom(p);
					for(Force f : forces)
						f.apply(p, temp);
					p.copyFrom(p);
				}
			}
		}
		if(theParticles.isEmpty()) {
			scene.removeDrawable(this);
			return true;
		}
		return false;
	}
	
	/**Causes a single particle to be generated from a random emitter or the systems origin if no emitters are pressent*/
	public void addParticle() {
		Emitter e = emitters.get((int) (emitters.size() * Math.random()));
		synchronized (theParticles) {
			theParticles.add( new Particle(e.generatePosition(), maxAge, ageNoise) );
		}
	}
	
	
	@Override
	public boolean isTransparent() {
		return true;
	}
	
	@Override
	public float[] getTransparentObjectPos() {
		return source.pos;
	}
	
	@FunctionalInterface
	public interface Emitter{
		public Vector3f generatePosition();
	}
	@FunctionalInterface
	public interface Collector{
		public boolean shouldCollect(Particle p);
	}
	@FunctionalInterface 
	public interface Force{
		public void apply(Particle from, Particle dest);
	}
	public static class Particle {
		Vector3f position, velocity, scale;
		Color color;
		long age, maxAge;
		private Particle() {
		}
		public Particle(Vector3f position, long maxAge, float maxAgeNoise) {
			this.position = position;
			velocity = new Vector3f();
			scale = new Vector3f(1, 1, 1);
			color = Color.WHITE.clone();
			age = 0;
			this.maxAge = (long) (maxAge * maxAgeNoise*(2*Math.random()-1));
		}
		public void copyFrom(Particle other) {
			this.position.set(other.position);
			this.velocity.set(other.velocity);
			this.scale.set(other.scale);
			this.color = other.color.clone();
			this.age = other.age;
			this.maxAge = other.maxAge;
		}
	}
}
