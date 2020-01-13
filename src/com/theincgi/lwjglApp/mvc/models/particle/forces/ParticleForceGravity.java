package com.theincgi.lwjglApp.mvc.models.particle.forces;

import com.theincgi.lwjglApp.mvc.models.particle.Particle;

/**Standard gravity*/
public class ParticleForceGravity implements ParticleForce{
	@Override
	public void apply(Particle source, Particle forceAcumulator) {
		forceAcumulator.velocity.y -= 9.81; 
	}
}
