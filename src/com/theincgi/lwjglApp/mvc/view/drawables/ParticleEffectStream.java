package com.theincgi.lwjglApp.mvc.view.drawables;

import java.util.ListIterator;
import java.util.Optional;

import org.lwjgl.util.vector.Matrix;
import org.lwjgl.util.vector.Vector3f;

import com.theincgi.lwjglApp.misc.MatrixStack;
import com.theincgi.lwjglApp.mvc.models.particle.ParticleCollector;
import com.theincgi.lwjglApp.mvc.models.particle.ParticleEmitter;
import com.theincgi.lwjglApp.mvc.models.particle.ParticleSystem;
import com.theincgi.lwjglApp.mvc.models.particle.forces.ParticleForce;
import com.theincgi.lwjglApp.mvc.models.particle.forces.ParticleForceSetSpawnVelocity;
import com.theincgi.lwjglApp.render.Drawable;
import com.theincgi.lwjglApp.render.Location;
import com.theincgi.lwjglApp.render.Model;
import com.theincgi.lwjglApp.render.ObjManager;
import com.theincgi.lwjglApp.render.shaders.ShaderProgram;
import com.theincgi.lwjglApp.ui.Scene;

public class ParticleEffectStream implements Drawable {
	Location source;
	ParticleSystem pSys;
	private Scene scene;
	private static Optional<Model> particleModel = Optional.empty();
	
	public ParticleEffectStream(Location source, Scene scene) {
		this.source = source;
		this.scene = scene;
		pSys = new ParticleSystem(new ParticleEmitter[] {
				()->{
					return new Vector3f();
				}
		}, new ParticleCollector[] {}, new ParticleForce[] {
				new ParticleForceSetSpawnVelocity()
		}, 1000, 1000); 
		
		if(particleModel.isEmpty())
			particleModel = ObjManager.INSTANCE.get("cmodels/softcube/softcube.obj"/*"cmodels/plane/1cm_square.obj"*/,"particle");
	}
	
	public void emit() {
		pSys.beginEmission(scene, 1000, 10);
	}

	@Override
	public void draw() {
		particleModel.ifPresent(model->{
			try(MatrixStack ms = MatrixStack.modelViewStack.pushTransform(source)){
				pSys.forEach(c->{
					model.shader.ifPresent(shader->{
						shader.bind();
						shader.trySetMatrix("viewMatrix", MatrixStack.view.get());
						shader.trySetMatrix("modelViewMatrix", MatrixStack.modelViewStack.get());
						shader.trySetUniform("particlePosition", c.position);
						shader.trySetUniform("particleColor", 	 c.color);
						shader.trySetUniform("particleScale", 	 c.scale);
						shader.trySetUniform("particleVelocity", c.velocity);
					});
					model.drawAtOrigin();
				});
			}
		});
	}

	@Override
	public boolean isTransparent() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public float[] getTransparentObjectPos() {
		// TODO Auto-generated method stub
		return source.pos;
	}
}
