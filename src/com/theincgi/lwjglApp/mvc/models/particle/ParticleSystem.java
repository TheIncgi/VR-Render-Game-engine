package com.theincgi.lwjglApp.mvc.models.particle;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.function.Consumer;

import org.lwjgl.util.vector.Vector3f;

import com.theincgi.lwjglApp.misc.Logger;
import com.theincgi.lwjglApp.misc.Tickable;
import com.theincgi.lwjglApp.mvc.models.particle.forces.ParticleForce;
import com.theincgi.lwjglApp.render.Location;
import com.theincgi.lwjglApp.ui.Color;
import com.theincgi.lwjglApp.ui.Scene;

public class ParticleSystem implements Tickable{
	ParticleEmitter[] emitters;
	ParticleCollector[] collectors; 
	ParticleForce[] forces;
	long minimumParticleLife;
	long maximumParticleLife;
	int nBorn, maxSpawn;
	private LinkedList<Particle> active = new LinkedList<>();
	private int activeParticleCount;
	private long spawnEnd, spawnStart;
	
	public ParticleSystem(ParticleEmitter[] emitters, ParticleCollector[] collectors,
			ParticleForce[] forces, long minimumParticleLife, long maximumParticleLife) {
		this.emitters = emitters;
		this.collectors = collectors;
		this.forces = forces;
		this.minimumParticleLife = minimumParticleLife;
		this.maximumParticleLife = maximumParticleLife;
	}
	
	/**Adds self to update list and begins emiting particles
	 * @param the scene to render particles in
	 * @param spawnTime how long to emit the spawned particles over in milliseconds*/
	public void beginEmission(Scene scene, long spawnTime, int toSpawn) {
		this.spawnStart = System.currentTimeMillis();
		this.spawnEnd = spawnStart + spawnTime;
		maxSpawn = toSpawn;
		nBorn = 0;
		scene.addTickable(this);
	}
	
	/** Starts an endless particle system where dead particles are recycled
	 * Adds self to update list and begins emiting particles
	 * @param particleCount how many particles to render */
	public void beginEmission(Scene scene, int toSpawn) {
		Logger.preferedLogger.w("ParticleSystem#beginEmission", "continuous emission mode not implemented yet");
	}
	
	@Override
	public synchronized boolean onTickUpdate() {
		long now = System.currentTimeMillis();
		
		int targetBorn = Math.min(maxSpawn, (int) ((now - spawnStart)/(float)(spawnEnd - spawnStart)*maxSpawn));
		Particle temp = new Particle();
		while(nBorn++ < targetBorn)
			addParticle(now);
		ListIterator<Particle> iterator = active.listIterator();
		while(iterator.hasNext()) {
			Particle p = iterator.next();
			if(p.expiration > now) {
				iterator.remove();
				continue;
			}
			for (ParticleCollector c : collectors) {
				if(c.shouldDestroy(p)) {
					iterator.remove();
					continue;
				}
			}
			temp.copyFrom(p);
			for (ParticleForce f : forces) {
				f.apply(p, temp);
			}
			p.copyFrom(temp);
			p.justBorn = false;
			Vector3f.add(p.position, p.velocity, p.position);
		}
		return active.isEmpty();
	}

	protected void addParticle(long now) {
		ParticleEmitter e = emitters[(int) (Math.random()*emitters.length)];
		Particle p = new Particle((long) (Math.random()*(maximumParticleLife-minimumParticleLife)+minimumParticleLife), now, e.generateSpawnPosition(), new Vector3f(), 0, Color.WHITE.clone());
		active.add(p);
	}
	
	public synchronized void forEach(Consumer<Particle> forEach) {
		active.forEach(forEach);
	}
}
