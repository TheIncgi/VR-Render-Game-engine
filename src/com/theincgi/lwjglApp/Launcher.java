package com.theincgi.lwjglApp;

import java.awt.Font;
import java.io.IOException;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;


import com.theincgi.lwjglApp.misc.Logger;
import com.theincgi.lwjglApp.misc.ObjCompresser;
import com.theincgi.lwjglApp.misc.Settings;
import com.theincgi.lwjglApp.render.text.FontTextures;
import com.theincgi.lwjglApp.render.vr.VRUtil;
import com.theincgi.lwjglApp.scenes.DemoScene;
import com.theincgi.lwjglApp.ui.AWindow;
import com.theincgi.lwjglApp.ui.VRWindow;
import com.theincgi.lwjglApp.ui.Window;

public class Launcher {
	public static final String APPLICATION_NAME = "Unnamed Application";
	private static AWindow mainWindow;
	static Logger log = Logger.preferedLogger;
	static {
		Settings.load();
		Settings.save();
		FontTextures.fontsFolder.mkdirs();
		if(FontTextures.fontsFolder.list().length==0) {
			FontTextures.generate("ascii_",new Font("consolas", Font.PLAIN, 16), 0, 255);
			FontTextures.generate("ascii_",new Font("consolas", Font.PLAIN, 32), 0, 255);
			FontTextures.generate("ascii_",new Font("consolas", Font.PLAIN, 64), 0, 255);
		}
		FontTextures.updateFontList();
	}
	private static VRUtil vrUtil;
	public static void main(String[] args) {
		try {
			Logger.preferedLogger.i("Launcher#main", "Checking for new models");
			ObjCompresser.compressAll(false);
		} catch (IOException e) {
			Logger.preferedLogger.e("launcher#static", e);
		}
		try(VRUtil util = new VRUtil()){
			vrUtil = util;
			util.initVR();
			log.i("#main", "Main thread: "+Thread.currentThread().getId());
			mainWindow = new VRWindow(600, 500, APPLICATION_NAME, null);
			mainWindow.setScene(new DemoScene(mainWindow));
			GLFW.glfwSetErrorCallback((err, desc)->{
				log.w("GL_ERROR", "["+Integer.toHexString(err)+ "]: "+ GLFWErrorCallback.getDescription(desc));
			});

			mainWindow.show();
		}
	}

	public static AWindow getMainWindow() {
		return mainWindow;
	}
	public static VRUtil getVrUtil() {
		return vrUtil;
	}
}


/*
 * TODO
 * Custom shaders: http://wiki.lwjgl.org/wiki/GLSL_Shaders_with_LWJGL.html
 * Test shaders in custom enviroment
 * */
