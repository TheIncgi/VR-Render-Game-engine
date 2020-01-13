package com.theincgi.lwjglApp.mvc.models.particle;

import org.lwjgl.util.vector.Vector3f;

@FunctionalInterface
public interface ParticleEmitter {
	public Vector3f generateSpawnPosition();
}
