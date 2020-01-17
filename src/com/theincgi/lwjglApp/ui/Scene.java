package com.theincgi.lwjglApp.ui;


import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.lwjgl.util.vector.Vector4f;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.*;

import com.theincgi.lwjglApp.Utils;
import com.theincgi.lwjglApp.misc.Logger;
import com.theincgi.lwjglApp.misc.MatrixStack;
import com.theincgi.lwjglApp.misc.RayCast;
import com.theincgi.lwjglApp.misc.Tickable;
import com.theincgi.lwjglApp.mvc.models.Bounds;
import com.theincgi.lwjglApp.render.Camera;
import com.theincgi.lwjglApp.render.Drawable;
import com.theincgi.lwjglApp.render.EyeCamera;
import com.theincgi.lwjglApp.render.Location;
import com.theincgi.lwjglApp.render.Side;
import com.theincgi.lwjglApp.render.shaders.ShaderManager;
import com.theincgi.lwjglApp.render.shaders.ShaderProgram;
import com.theincgi.lwjglApp.vrGUI.Gui;


public class Scene {
	public Color clearColor = Color.BLACK.clone();
	protected Optional<CallbackListener> sceneListener = Optional.empty(); 
	protected Location sun = new Location(3, 10, 4);
	protected Color sunColor = Color.WHITE.clone();

	long startupTime = System.currentTimeMillis();
	private AWindow window;

	protected final HashSet<Drawable> opaqueDrawables = new HashSet<>();
	protected final LinkedList<Drawable> transparentDrawables = new LinkedList<>();
	protected final HashSet<Tickable> tickables = new HashSet<>();

	public Scene(AWindow window) {
		this.window = window;
		if(window instanceof VRWindow) {
			VRWindow v = (VRWindow) window;
			addDrawable(v.vrControllers);
		}
	}

	public void render(Camera camera, double mouseX, double mouseY) {
		MatrixStack.modelViewStack.reset();
		MatrixStack.view.reset();

		//gluPickMatrix2((float)mouseX, (float)mouseY, 1, 1);

		camera.viewMatrix(); //load the view
		ShaderManager.INSTANCE.forLoaded(s->{
			camera.tellShader(s);
			s.bind();
			s.trySetUniform("uptime", (System.currentTimeMillis()-startupTime)/1000f); //casted to float
			s.trySetUniform("sunPos", sun.pos);
			s.trySetUniform("sunColor", sunColor.vec());
			s.trySetUniform("cameraPos", camera.getLocation().pos);
			s.trySetMatrix("projectionMatrix", camera.projectionMatrix());
			s.trySetMatrix("viewMatrix", MatrixStack.view.get());

		});
		ShaderProgram.unbind();
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_ALPHA);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		synchronized (opaqueDrawables) {
			opaqueDrawables.forEach(d->{
				d.draw();
				if(d.showBounds())
					d.getBounds().ifPresent(bounds->{
						bounds.draw();
					});
			});
		}
		synchronized (transparentDrawables) {
			if(!(camera instanceof EyeCamera) || ((EyeCamera)camera).getSide().equals(Side.LEFT)) { //only sort once per frame, difference between eyes should be minor
				//TODO optimize? HashMap<Drawable, Float> farthestFirstDistanceCache?
				Collections.sort(transparentDrawables, (a,b)->{
					float d1 = Utils.distVec3(camera.getLocation().pos, a.getTransparentObjectPos());
					float d2 = Utils.distVec3(camera.getLocation().pos, b.getTransparentObjectPos());
					return Float.compare(d2, d1);
				});
			}
			transparentDrawables.forEach(d->{
				d.draw();
				if(d.showBounds())
					d.getBounds().ifPresent(bounds->{
						bounds.draw();
					});
			});
		}
	}
	
	/**Tries to find a hit for some ray in the list of drawable objects
	 * if multiple hit, the closest will be returned*/
	public void raycast(Drawable optIgnore, RayCast ray) {
		Vector4f best = new Vector4f();
		boolean found = false;
		synchronized (opaqueDrawables) {
			for(Drawable d : opaqueDrawables) {
				if(d==optIgnore) continue;
				if(d.getBounds().isPresent()) {
					Bounds b = d.getBounds().get();
					if(b.isRaycastPassthru(ray)) {
						if(ray.result==null)
							Logger.preferedLogger.w("Scene#raycast", "Drawable "+d+" returned true for raycast, but provided no result locaiton");
						else {
						if(best.length() > ray.result.length() || !found) {
							best.set(ray.result);
							ray.raycastedObject = d;
						}
						found = true;
					}
					}
				};
			}
			for(Drawable d : transparentDrawables) { //should be closest last
				if(d==optIgnore) continue;
				if(d.getBounds().isPresent()) {
					Bounds b = d.getBounds().get();
					if(b.isRaycastPassthru(ray)) {
						found = true;
						if(best.length() > ray.result.length()) {
							best.set(ray.result);
							ray.raycastedObject = d;
						}
					}
				};
			}
		}
		if(found)
			ray.result.set(best);
	}
	
	/**adds any drawables that are colliding with the target drawable<br>
	 * (will exclude itself from results)<br>
	 * if the target object does not have bounds then no results will be added*/
	public void findCollisions(Drawable self, List<Drawable> collisions) {
		if(self.getBounds().isEmpty()) return;
		Bounds b = self.getBounds().get();
		for(Drawable d : opaqueDrawables) {
			if(d==self) continue;
			d.getBounds().ifPresent(c->{
				if(b.intersects(c))
					collisions.add(d);
			});
		}
		for (Drawable d : transparentDrawables) {
			if(d==self) continue;
			d.getBounds().ifPresent(c->{
				if(b.intersects(c))
					collisions.add(d);
			});
		}
	}
	
	public void onTick() {}

	public Optional<CallbackListener> getSceneListener() {
		return sceneListener;
	}

	public void onUnload(){
	}

	public void addDrawable(Drawable obj) {
		if(!obj.isTransparent())
			synchronized (opaqueDrawables) {
				opaqueDrawables.add(obj);
			}
		else
			synchronized (transparentDrawables) {
				if(!transparentDrawables.contains(obj))
					transparentDrawables.add(obj);
			}
	} 
	public void addDrawables(Drawable...objs) {
		for (Drawable drawable : objs) {
			addDrawable(drawable);
		}
	}
	public boolean removeDrawable(Drawable obj) {
		synchronized (opaqueDrawables) {
			if(opaqueDrawables.remove(obj))
				return true;
		}
		synchronized (transparentDrawables) {
			return transparentDrawables.remove(obj);
		}
	}
	public void addTickable(Tickable t) {
		synchronized (tickables) {
			tickables.add( t );
		}
	}
	public void removeTickable(Tickable t) {
		synchronized (tickables) {
			tickables.remove( t );
		}
	}


	public Optional<Drawable> getClicked(float x, float y) {

		return Optional.empty();
	}




}
