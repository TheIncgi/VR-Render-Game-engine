package com.theincgi.lwjglApp.mvc.models.particle.forces;

import com.theincgi.lwjglApp.mvc.models.particle.Particle;

public class ParticleForceSetSpawnVelocity implements ParticleForce{
	@Override
	public void apply(Particle source, Particle forceAcumulator) {
		if(source.justBorn)
			forceAcumulator.velocity.set(0, (float) (Math.random()/8+.02), 0);
	}
}
