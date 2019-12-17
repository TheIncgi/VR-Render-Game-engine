package com.theincgi.lwjglApp.ui;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.*;

import java.io.File;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

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

public class Window {
	public final long WINDOW_HANDLE;
	ArrayList<CallbackListener> callbackListeners = new ArrayList<>();
	
	
	static {
		GLFWErrorCallback.createPrint(System.err).set();
		if ( !glfwInit() )
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

	}

	public Window(int wid, int hei, String title) {
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

		// Make the window visible
		glfwShowWindow(WINDOW_HANDLE);
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
			for (CallbackListener callbackListener : callbackListeners)
				if(callbackListener instanceof OnMouseButton)
					if(((OnMouseButton)callbackListener).onMouseButton(this, button, action, mods)) return;
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
}
