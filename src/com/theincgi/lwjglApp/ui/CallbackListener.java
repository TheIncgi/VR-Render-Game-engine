package com.theincgi.lwjglApp.ui;

import java.io.File;

public class CallbackListener {

	

	@FunctionalInterface
	public interface OnKey {
		public boolean onKey(Window window, int key, int scan, int action, int mods);
	}
	@FunctionalInterface
	public interface OnChar{
		public boolean onChar(Window window, int codepoint);
	}
	@FunctionalInterface
	public interface OnMouseEnter {
		public boolean onMouseEnter(Window window);
	}
	@FunctionalInterface
	public interface OnMouseExit{
		public boolean onMouseExit(Window window);
	}
	@FunctionalInterface
	public interface OnMousePos {
		public void onMousePos(Window window, double mouseX, double mouseY);
	}
	@FunctionalInterface
	public interface OnFilesDrop {
		public boolean onFilesDrop(Window window, File[] files);
	}
	@FunctionalInterface
	public interface OnResize {
		public void onResize(Window window, int width, int height);
	}
	@FunctionalInterface
	public interface OnJoystick {
		public boolean onJoystick(Window window, int jid, Object event); //TODO make specific
	}
	@FunctionalInterface
	public interface OnMouseButton {
		public boolean onMouseButton(Window window, double x, double y, int button, int action, int mods);
	}
	@FunctionalInterface
	public interface OnScroll {
		public boolean onScroll(Window window, double scrollX, double scrollY);
	}
	@FunctionalInterface
	public interface OnWindowClosed{
		public void onWindowClosed(Window window);
	}
	@FunctionalInterface
	public interface OnWindowFocus{
		public void onWindowFocus(Window window, boolean isFocused);
	}
	@FunctionalInterface
	public interface OnWindowIconified{
		public void onWindowIconified(Window window);
	}
	@FunctionalInterface
	public interface OnWindowRestored{
		public void onWindowNormalize(Window window);
	}
	@FunctionalInterface
	public interface OnWindowMaximized{
		public void onWindowNormalized(Window window);
	}
	@FunctionalInterface
	public interface OnWindowNormalized{
		public void onWindowNormalized(Window window);
	}
	@FunctionalInterface
	public interface OnWindowMoved{
		public void onWindowMoved(Window window, int windowX, int windowY);
	}
}
