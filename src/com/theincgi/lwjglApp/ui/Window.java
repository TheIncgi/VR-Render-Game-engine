package com.theincgi.lwjglApp.ui;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.system.MemoryStack.*;

import java.io.File;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Optional;


import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import com.theincgi.lwjglApp.misc.Logger;
import com.theincgi.lwjglApp.misc.Pair;
import com.theincgi.lwjglApp.render.Camera;
import com.theincgi.lwjglApp.render.text.TextRenderer;
import com.theincgi.lwjglApp.ui.CallbackListener.OnChar;
import com.theincgi.lwjglApp.ui.CallbackListener.OnFilesDrop;
import com.theincgi.lwjglApp.ui.CallbackListener.OnJoystick;
import com.theincgi.lwjglApp.ui.CallbackListener.OnKey;
import com.theincgi.lwjglApp.ui.CallbackListener.OnMouseButton;
import com.theincgi.lwjglApp.ui.CallbackListener.OnMouseEnter;
import com.theincgi.lwjglApp.ui.CallbackListener.OnMouseExit;
import com.theincgi.lwjglApp.ui.CallbackListener.OnMousePos;
import com.theincgi.lwjglApp.ui.CallbackListener.OnResize;
import com.theincgi.lwjglApp.ui.CallbackListener.OnScroll;
import com.theincgi.lwjglApp.ui.CallbackListener.OnWindowClosed;
import com.theincgi.lwjglApp.ui.CallbackListener.OnWindowFocus;
import com.theincgi.lwjglApp.ui.CallbackListener.OnWindowIconified;
import com.theincgi.lwjglApp.ui.CallbackListener.OnWindowMaximized;
import com.theincgi.lwjglApp.ui.CallbackListener.OnWindowMoved;


import org.lwjgl.*;

public class Window extends AWindow{
	private Camera camera;


	public Window(int wid, int hei, String title, Scene scene) {
		super(wid, hei, title, scene);
	}


	protected void postInit() {
		camera = new Camera(0,0,-7);
		TextRenderer.init();
	}

	
	protected void loop(){
		// Run the rendering loop until the user has attempted to close
		// the window
		while ( !glfwWindowShouldClose(WINDOW_HANDLE) ) {
			setViewport(0, 0, width, height);
			scene.ifPresentOrElse(value->{
				Color cc = value.clearColor;
				glClearColor(cc.r(), cc.g(), cc.b(), cc.a());
				glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
				Pair<Double, Double> mousePos = getMousePos();
				value.render(camera, mousePos.x, mousePos.y);
			}, /*else*/()->{
				glClearColor(0,0,0,0);
				glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			});
			// clear the framebuffer

			glfwSwapBuffers(WINDOW_HANDLE); // swap the color buffers

			glfwPollEvents();
		}
	}
	
	@Override
	public Optional<Window> getWindow() {
		return Optional.of(this);
	}
	@Override
	public Optional<VRWindow> getVRWindow() {
		return Optional.empty();
	}
	@Override
	public boolean isVR() {
		return false;
	}
}
