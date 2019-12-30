package com.theincgi.lwjglApp.ui;


import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.vector.Matrix4f;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL45.*;

import com.theincgi.lwjglApp.Launcher;
import com.theincgi.lwjglApp.misc.MatrixStack;
import com.theincgi.lwjglApp.render.Camera;
import com.theincgi.lwjglApp.render.Drawable;
import com.theincgi.lwjglApp.render.Location;
import com.theincgi.lwjglApp.render.shaders.ShaderManager;
import com.theincgi.lwjglApp.render.shaders.ShaderProgram;


public class Scene {
	public Color clearColor = Color.SKY.clone();
	protected Optional<CallbackListener> sceneListener = Optional.empty(); 
	Location sun = new Location(3, 10, 4);
	Color sunColor = Color.WHITE.clone();
	public Camera camera;
	long startupTime = System.currentTimeMillis();
	private Window window;

	private final LinkedList<Drawable> drawables = new LinkedList<>();

	public Scene(Window window) {
		this.window = window;
		camera = new Camera();

	}

	public void render(double mouseX, double mouseY) {
		MatrixStack.modelViewStack.reset();
		MatrixStack.projection.reset();
		
		//gluPickMatrix2((float)mouseX, (float)mouseY, 1, 1);
		camera.loadProjectionMatrix();

		ShaderManager.INSTANCE.forLoaded(s->{
			camera.tellShader(s);
			s.bind();
			s.trySetUniform("uptime", (System.currentTimeMillis()-startupTime)/1000f); //casted to float
			s.trySetUniform("sunPos", sun.pos);
			s.trySetUniform("sunColor", sunColor.vec());
			s.trySetUniform("cameraPos", camera.getLocation().pos);
			s.trySetMatrix("projectionMatrix", MatrixStack.projection.get());

		});
		ShaderProgram.unbind();
		glEnable(GL_DEPTH_TEST);

		drawables.forEach(d->{
			d.draw();
		});
	}

	public Optional<CallbackListener> getSceneListener() {
		return sceneListener;
	}

	public void onUnload(){

	}

	public void addDrawable(Drawable obj) {
		synchronized (drawables) {
			drawables.add(obj);
		}
	} 
	public void addDrawables(Drawable...objs) {
		synchronized (drawables) {
			Collections.addAll(drawables, objs);
		}
	}
	public boolean removeDrawable(Drawable obj) {
		synchronized (drawables) {
			return drawables.remove(obj);
		}
	}



	public Optional<Drawable> getClicked(float x, float y) {
		
		return Optional.empty();
	}



}
