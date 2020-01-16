package com.theincgi.lwjglApp.render;

import static org.lwjgl.opengl.GL11.GL_DST_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Optional;

import org.lwjgl.util.vector.Vector3f;

import com.theincgi.lwjglApp.misc.MatrixStack;
import com.theincgi.lwjglApp.misc.Tickable;
import com.theincgi.lwjglApp.mvc.models.Bounds;
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
	private Optional<ImgTexture> img = Optional.empty();

	/**Registers self to drawable and tickable listeners when `emit()` is called, removes self when no particles remain*/
	public ParticleSystem(Scene scene, float x, float y, float z) {
		this.scene = scene;
		this.source = new Location(x,y,z);
		if(particle.isEmpty())
			particle = ObjManager.INSTANCE.get("cmodels/plane/1cm_square.obj", "particle");
		addCollector(p->{
			return p.age >= p.maxAge; 
		});
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
		scene.addDrawable(this);
		scene.addTickable(this);
	}

	public ParticleSystem setTexture(String textureFile) {
		this.img = TextureManager.INSTANCE.get(textureFile);
		return this;
	}
	public ParticleSystem setTexture(File textureFile) {
		this.img = TextureManager.INSTANCE.get(textureFile);
		return this;
	}
	public ParticleSystem setTexture(ImgTexture optionalTexture) {
		this.img = Optional.ofNullable(optionalTexture);
		return this;
	}
	public ParticleSystem setTexture(Optional<ImgTexture> texture) {
		this.img = texture;
		return this;
	}

	public ParticleSystem addEmitter(Emitter e) {
		synchronized (emitters) {
			emitters.add(e);
		}
		return this;
	}
	public ParticleSystem addForce(Force e) {
		synchronized (forces) {
			forces.add(e);
		}
		return this;
	}
	public ParticleSystem addCollector(Collector e) {
		synchronized (collectors) {
			collectors.add(e);
		}
		return this;
	}

	@Override
	public void draw() {
		particle.ifPresent(pm->{
			pm.shader.ifPresent(s->{
				s.bind();
				glBlendFunc(GL_SRC_ALPHA, GL_DST_ALPHA);
				int flags = 0;
				if(img.isPresent()) {
					s.trySetUniformTexture("particleTexture", img.get(), 0);
					flags |= 1;
				}
				
				
				s.trySetUniform("flags", flags);
				synchronized (theParticles) {
					theParticles.forEach(p->{
						s.trySetUniform("particleScale", 	p.scale		);
						s.trySetUniform("particleColor", 	p.color		);
						s.trySetUniform("particleVelocity", p.velocity	);
						s.trySetUniform("particlePosition", p.position	);
						s.trySetUniform("particleAge", 		p.age		);
						s.trySetUniform("particleMaxAge", 	p.maxAge	);
						s.trySetUniform("particleEmissionStrength", 	p.emissionStrength	);
						try(MatrixStack ms = MatrixStack.modelViewStack.push()){
							ms.get().translate(p.position);
							pm.drawAtOrigin();
						}
					});
				}});
		});

	}

	@Override
	public boolean onTickUpdate() {
		long now = System.currentTimeMillis();
		int target = (int) Math.min(spawnAmount, (now-startTime)* spawnAmount / timeSpan);
		long animationAge = now-startTime;
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
						f.apply(animationAge, p, temp);
					p.copyFrom(temp);
				}
				Vector3f.add(p.position, p.velocity, p.position);
				p.age++; //happy birthday...again
			}
		}
		if(theParticles.isEmpty() && now > startTime+timeSpan) {
			scene.removeDrawable(this);
			return true;
		}
		return false;
	}

	/**Causes a single particle to be generated from a random emitter or the systems origin if no emitters are pressent*/
	public void addParticle() {
		Particle p;
		synchronized (emitters) {
			if(emitters.size()==0)
				p = new Particle(new Vector3f(source.getX(), source.getY(), source.getZ()), maxAge, ageNoise);
			else {
				Emitter e = emitters.get((int) (emitters.size() * Math.random()));
				p = new Particle(e.generatePosition(), maxAge, ageNoise);
			}
		}
		synchronized (theParticles) {
			theParticles.add(p);
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
		public void apply(long animationAge, Particle from, Particle dest);
	}
	public static class Particle {
		public Vector3f position;
		public Vector3f velocity;
		public Vector3f scale;
		public Color color;
		public float emissionStrength = 0;
		public long age, maxAge;
		private Particle() {
			position = new Vector3f();
			velocity = new Vector3f();
			scale    = new Vector3f();
		}
		public Particle(Vector3f position, long maxAge, float maxAgeNoise) {
			this.position = position;
			velocity = new Vector3f();
			scale = new Vector3f(8, 8, 8);
			color = Color.WHITE.clone();
			age = 0;
			this.maxAge = (long) (maxAge + maxAgeNoise*(2*Math.random()-1));
		}
		public void copyFrom(Particle other) {
			this.position.set(other.position);
			this.velocity.set(other.velocity);
			this.scale.set(other.scale);
			this.color = other.color.clone();
			this.age = other.age;
			this.maxAge = other.maxAge;
			this.emissionStrength = other.emissionStrength;
		}
	}
	
	@Override
	public Optional<Bounds> getBounds() {
		return Optional.empty();
	}
}
