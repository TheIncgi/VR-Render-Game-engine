package com.theincgi.lwjglApp.mvc.models.particle.forces;

import com.theincgi.lwjglApp.mvc.models.particle.Particle;

@FunctionalInterface
public interface ParticleForce {
	/**Function should not alter source particle directly*/
	void apply(final Particle source, final Particle forceAcumulator);

}
