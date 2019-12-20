package com.theincgi.lwjglApp.ui;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.system.MemoryStack.*;

import java.io.File;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Optional;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import com.theincgi.lwjglApp.misc.Logger;
import com.theincgi.lwjglApp.misc.Pair;
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

public class Window {
	public final long WINDOW_HANDLE;
	ArrayList<CallbackListener> callbackListeners = new ArrayList<>();
	private Optional<Scene> scene;

	static {
		Logger.consoleLogger.i("Window","LWJGL Version:  " + Version.getVersion());

		GLFWErrorCallback.createPrint(System.err).set();
		if ( !glfwInit() )
			throw new IllegalStateException("Unable to initialize GLFW");

	}

	/**
	 * Creates a new window with the given size, title and scene
	 * @param wid width of the new window
	 * @param hei height of the new window
	 * @param title title of the new window
	 * @param scene scene to render in the window, may be null
	 * */
	public Window(int wid, int hei, String title, Scene scene) {
		this.scene = Optional.ofNullable(scene);

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 5);

		WINDOW_HANDLE = glfwCreateWindow(wid, hei, title, 0, 0); //NULL is 0
		if ( WINDOW_HANDLE == 0 )
			throw new RuntimeException("Failed to create the GLFW window");

		setupCallbacks();

		// Get the thread stack and push a new frame
		try ( MemoryStack stack = stackPush() ) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(WINDOW_HANDLE, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(
					WINDOW_HANDLE,
					(vidmode.width() - pWidth.get(0)) / 2,
					(vidmode.height() - pHeight.get(0)) / 2
					);
		} // the stack frame is popped automatically

		// Make the OpenGL context current
		glfwMakeContextCurrent(WINDOW_HANDLE);
		// Enable v-sync
		glfwSwapInterval(1);
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();

	}

	public void show() {
		// Make the window visible
		glfwShowWindow(WINDOW_HANDLE);

		loop();
	}

	public void setScene(Scene s) {
		this.scene.ifPresent(v->{
			v.onUnload();
			v.getSceneListener().ifPresent(u->{
				callbackListeners.remove(u);
			});
		});
		this.scene = Optional.ofNullable(s);
		this.scene.ifPresent(v->{
			v.getSceneListener().ifPresent(u->{
				callbackListeners.add(u);
			});
		});
	}
	public void close() {
		glfwSetWindowShouldClose(WINDOW_HANDLE, true);
	}

	private void loop() {


		// Run the rendering loop until the user has attempted to close
		// the window
		while ( !glfwWindowShouldClose(WINDOW_HANDLE) ) {
			scene.ifPresentOrElse(value->{
				Color cc = value.clearColor;
				glClearColor(cc.r(), cc.g(), cc.b(), cc.a());
				glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
				Pair<Double, Double> mousePos = getMousePos();
				value.render(mousePos.x, mousePos.y);
			}, /*else*/()->{
				glClearColor(0,0,0,0);
				glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			});
			// clear the framebuffer

			glfwSwapBuffers(WINDOW_HANDLE); // swap the color buffers

			// Poll for window events. The key callback above will only be
			// invoked during this call.
			glfwPollEvents();
		}
	}



	private void setupCallbacks() {
		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		glfwSetKeyCallback(WINDOW_HANDLE, (window, key, scancode, action, mods) -> {
			for (CallbackListener callbackListener : callbackListeners) {
				if (callbackListener instanceof OnKey) {
					OnKey onKey = (OnKey) callbackListener;
					if(onKey.onKey(this, key, scancode, action, mods)) return;
				}
			}
		});
		glfwSetCharCallback(WINDOW_HANDLE, (window, codepoint)->{
			for (CallbackListener callbackListener : callbackListeners) {
				if (callbackListener instanceof OnChar) {
					OnChar onChar = (OnChar) callbackListener;
					if(onChar.onChar(this, codepoint)) return;
				}
			}
		});
		glfwSetCursorEnterCallback(WINDOW_HANDLE, (window, entered)->{
			for (CallbackListener callbackListener : callbackListeners) {
				if (entered && callbackListener instanceof OnMouseEnter) {
					if(((OnMouseEnter) callbackListener).onMouseEnter(this)) return;
				}else if((!entered) && callbackListener instanceof OnMouseExit)
					if(((OnMouseExit)callbackListener).onMouseExit(this)) return;
			}
		});
		glfwSetCursorPosCallback(WINDOW_HANDLE, (window, x, y)->{
			for (CallbackListener callbackListener : callbackListeners) {
				if(callbackListener instanceof OnMousePos)
					((OnMousePos)callbackListener).onMousePos(this, x, y);
			}
		});
		glfwSetDropCallback(WINDOW_HANDLE, (window, count, names)->{
			PointerBuffer nameBuffer = MemoryUtil.memPointerBuffer(names, count);
			File[] files = new File[count];
			for ( int i = 0; i < count; i++ ) {
				files[i] = new File(MemoryUtil.memUTF8(MemoryUtil.memByteBufferNT1(nameBuffer.get(i))));
			}
			for (CallbackListener callbackListener : callbackListeners) {
				if (callbackListener instanceof OnFilesDrop)
					if(((OnFilesDrop)callbackListener).onFilesDrop(this, files)) return;
			}
		});
		glfwSetFramebufferSizeCallback(WINDOW_HANDLE, (window, wid, hei)->{
			for (CallbackListener callbackListener : callbackListeners) {
				if(callbackListener instanceof OnResize)
					((OnResize)callbackListener).onResize(this, wid, hei);
			}
		});
		glfwSetJoystickCallback((jid, event)->{
			for (CallbackListener callbackListener : callbackListeners) {
				if(callbackListener instanceof OnJoystick)
					if(((OnJoystick)callbackListener).onJoystick(this, jid, event)) return;
			}
		});
		glfwSetMouseButtonCallback(WINDOW_HANDLE, (window, button, action, mods)->{
			Pair<Double, Double> mpos = getMousePos();
			for (CallbackListener callbackListener : callbackListeners)
				if(callbackListener instanceof OnMouseButton)
					if(((OnMouseButton)callbackListener).onMouseButton(this,mpos.x, mpos.y, button, action, mods)) return;
		});
		glfwSetScrollCallback(WINDOW_HANDLE, (window, dx, dy)->{
			for (CallbackListener callbackListener : callbackListeners)
				if(callbackListener instanceof OnScroll)
					if(((OnScroll)callbackListeners).onScroll(this, dx, dy)) return;
		});
		glfwSetWindowCloseCallback(WINDOW_HANDLE, (window)->{
			for (CallbackListener callbackListener : callbackListeners) 
				if(callbackListener instanceof OnWindowClosed)
					((OnWindowClosed)callbackListeners).onWindowClosed(this); 
		});
		glfwSetWindowFocusCallback(WINDOW_HANDLE, (window, focused)->{
			for (CallbackListener callbackListener : callbackListeners)
				if(callbackListener instanceof OnWindowFocus)
					((OnWindowFocus)callbackListeners).onWindowFocus(this, focused);
		});
		glfwSetWindowIconifyCallback(WINDOW_HANDLE, (window, isIconified)->{
			for (CallbackListener callbackListener : callbackListeners)
				if(callbackListener instanceof OnWindowFocus)
					((OnWindowIconified)callbackListeners).onWindowIconified(this);
		});
		glfwSetWindowMaximizeCallback(WINDOW_HANDLE, (window, maximized)->{
			for (CallbackListener callbackListener : callbackListeners)
				if(callbackListener instanceof OnWindowFocus)
					((OnWindowMaximized)callbackListeners).onWindowNormalized(this);
		});
		glfwSetWindowPosCallback(WINDOW_HANDLE, (window, x, y)->{
			for (CallbackListener callbackListener : callbackListeners)
				if(callbackListener instanceof OnWindowFocus)
					((OnWindowMoved)callbackListeners).onWindowMoved(this, x, y);
		});
	}

	public Pair<Double, Double> getMousePos() {
		try( MemoryStack stack = stackPush()){
			DoubleBuffer xBuffer = stack.mallocDouble(1);
			DoubleBuffer yBuffer = stack.mallocDouble(1);
			glfwGetCursorPos(WINDOW_HANDLE, xBuffer, yBuffer);
			return new Pair<>(xBuffer.get(0), yBuffer.get(0));
		}
	}
	public Pair<Integer, Integer> getBufferSize() {
		try ( MemoryStack stack = stackPush() ) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*
			glfwGetWindowSize(WINDOW_HANDLE, pWidth, pHeight);
			return new Pair<Integer, Integer>(pWidth.get(0), pHeight.get(0));		
		}
	}
	public Pair<Integer, Integer> getWindowPos() {
		try ( MemoryStack stack = stackPush() ) {
			IntBuffer px = stack.mallocInt(1); // int*
			IntBuffer py = stack.mallocInt(1); // int*
			glfwGetWindowPos(WINDOW_HANDLE, px, py);
			return new Pair<Integer, Integer>(px.get(0), py.get(0));		
		}
	}

	@Override
	public String toString() {
		Pair<Integer, Integer> pos = getWindowPos(), size = getBufferSize();
		return String.format("Window: [#%d | Pos: <%d, %d> | BufSize: <%d, %d>]", WINDOW_HANDLE, pos.x, pos.y, size.x, size.y);
	}
}
