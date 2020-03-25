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

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.*;

import com.theincgi.lwjglApp.Utils;
import com.theincgi.lwjglApp.misc.Logger;
import com.theincgi.lwjglApp.misc.MatrixStack;
import com.theincgi.lwjglApp.misc.RayCast;
import com.theincgi.lwjglApp.misc.Tickable;
import com.theincgi.lwjglApp.mvc.models.Bounds;
import com.theincgi.lwjglApp.mvc.models.Colideable;
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
			s.bind();
			camera.tellShader(s);
			s.trySetUniform("uptime", (System.currentTimeMillis()-startupTime)/1000f); //casted to float
			s.trySetUniform("sunPos", sun.pos);
			s.trySetUniform("sunColor", sunColor.vec());
//			s.trySetUniform("cameraPos", camera.getLocation().pos);
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
			});
		}
	}

	/**Tries to find a hit for some ray in the list of drawable objects
	 * if multiple hit, the closest will be returned*/
	public void raycast(Colideable optIgnore, RayCast ray) {
		synchronized (opaqueDrawables) {
			for(Drawable d : opaqueDrawables) {
				if(d==optIgnore) continue;
				if(d instanceof Colideable) {
					Colideable c = (Colideable)d;
					if(!c.allowRaytraceHits())continue;
					Bounds b = c.getBounds();
					b.isRaycastPassthru(ray);
				}
			}
			for(Drawable d : transparentDrawables) { //should be closest last
				if(d==optIgnore) continue;
				if(d instanceof Colideable) {
					Colideable c = (Colideable)d;
					if(!c.allowRaytraceHits())continue;
					Bounds b = c.getBounds();
					b.isRaycastPassthru(ray);
				}
			}
		}

	}

	/**adds any drawables that are colliding with the target drawable<br>
	 * (will exclude itself from results)<br>
	 * if the target object does not have bounds then no results will be added*/
	public void findCollisions(Drawable self, List<Drawable> collisions) {
		if(!(self instanceof Colideable)) return;
		Colideable c = (Colideable) self;
		Bounds b = c.getBounds();
		for(Drawable d : opaqueDrawables) {
			if(d instanceof Colideable) {
				Colideable dc = (Colideable) d;
				if(dc==self) continue;
				if(dc.getBounds().intersects(c.getBounds()))
					collisions.add(d);
			}
		}
		for (Drawable d : transparentDrawables) {
			if(d instanceof Colideable) {
				Colideable dc = (Colideable) d;
				if(dc==self) continue;
				if(dc.getBounds().intersects(c.getBounds()))
					collisions.add(d);
			}
		}
	}

	public void onTick() {
		synchronized (tickables) {
			LinkedList<Tickable> toRemove = new LinkedList<>();
			tickables.forEach(t->{
				if(t.onTickUpdate())
					toRemove.add(t);
			});
			while(!toRemove.isEmpty())
				tickables.remove(toRemove.removeFirst());
		}
	}

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
