package com.theincgi.lwjglApp.ui;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;



import org.lwjgl.*;

import com.theincgi.lwjglApp.misc.Pair;
import com.theincgi.lwjglApp.render.EyeCamera;
import com.theincgi.lwjglApp.render.EyeSide;

public class VRWindow extends AWindow{
	EyeCamera leftEye, rightEye;


	public VRWindow(int wid, int hei, String title, Scene scene) {
		super(wid, hei, title, scene);
	}
	
	@Override
	protected void postInit() {
		leftEye = new EyeCamera();
		rightEye = new EyeCamera().setSide(EyeSide.RIGHT);
	}
	
	
	@Override
	void loop() {
		while ( !glfwWindowShouldClose(WINDOW_HANDLE) ) {
			setViewport(0, 0, width, height);
			scene.ifPresentOrElse(value->{
				Color cc = value.clearColor;
				glClearColor(cc.r(), cc.g(), cc.b(), cc.a());
				glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
				Pair<Double, Double> mousePos = getMousePos();
				value.render(leftEye, mousePos.x, mousePos.y);
			}, /*else*/()->{
				glClearColor(0,0,0,0);
				glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			});
			// clear the framebuffer

			glfwSwapBuffers(WINDOW_HANDLE); // swap the color buffers

			glfwPollEvents();
		}
	}
}
