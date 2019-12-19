package com.theincgi.lwjglApp;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;


import com.theincgi.lwjglApp.misc.Logger;
import com.theincgi.lwjglApp.scenes.PrimaryScene;
import com.theincgi.lwjglApp.ui.Window;

public class Launcher {
	public static final String APPLICATION_NAME = "Unnamed Application";
	private static Window mainWindow;
	static Logger log = Logger.consoleLogger;
	
	public static void main(String[] args) {
		log.i("#main", "Main thread: "+Thread.currentThread().getId());
		mainWindow = new Window(720, 440, APPLICATION_NAME, null);
		mainWindow.setScene(new PrimaryScene());
		GLFW.glfwSetErrorCallback((err, desc)->{
			log.w("GL_ERROR", "["+Integer.toHexString(err)+ "]: "+ GLFWErrorCallback.getDescription(desc));
		});
		mainWindow.show();
	}

	public static Window getMainWindow() {
		return mainWindow;
	}
}


/*
 * TODO
 * Custom shaders: http://wiki.lwjgl.org/wiki/GLSL_Shaders_with_LWJGL.html
 * Test shaders in custom enviroment
 * */
