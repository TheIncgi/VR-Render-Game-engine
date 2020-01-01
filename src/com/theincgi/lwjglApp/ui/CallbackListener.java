package com.theincgi.lwjglApp.ui;

import java.io.File;

public class CallbackListener {

	

	@FunctionalInterface
	public interface OnKey {
		public boolean onKey(AWindow window, int key, int scan, int action, int mods);
	}
	@FunctionalInterface
	public interface OnChar{
		public boolean onChar(AWindow window, int codepoint);
	}
	@FunctionalInterface
	public interface OnMouseEnter {
		public boolean onMouseEnter(AWindow window);
	}
	@FunctionalInterface
	public interface OnMouseExit{
		public boolean onMouseExit(AWindow window);
	}
	@FunctionalInterface
	public interface OnMousePos {
		public void onMousePos(AWindow window, double mouseX, double mouseY);
	}
	@FunctionalInterface
	public interface OnFilesDrop {
		public boolean onFilesDrop(AWindow window, File[] files);
	}
	@FunctionalInterface
	public interface OnResize {
		public void onResize(AWindow window, int width, int height);
	}
	@FunctionalInterface
	public interface OnJoystick {
		public boolean onJoystick(AWindow window, int jid, Object event); //TODO make specific
	}
	@FunctionalInterface
	public interface OnMouseButton {
		public boolean onMouseButton(AWindow window, double x, double y, int button, int action, int mods);
	}
	@FunctionalInterface
	public interface OnScroll {
		public boolean onScroll(AWindow window, double scrollX, double scrollY);
	}
	@FunctionalInterface
	public interface OnWindowClosed{
		public void onWindowClosed(AWindow window);
	}
	@FunctionalInterface
	public interface OnWindowFocus{
		public void onWindowFocus(AWindow window, boolean isFocused);
	}
	@FunctionalInterface
	public interface OnWindowIconified{
		public void onWindowIconified(AWindow window);
	}
	@FunctionalInterface
	public interface OnWindowRestored{
		public void onWindowNormalize(AWindow window);
	}
	@FunctionalInterface
	public interface OnWindowMaximized{
		public void onWindowNormalized(AWindow window);
	}
	@FunctionalInterface
	public interface OnWindowNormalized{
		public void onWindowNormalized(AWindow window);
	}
	@FunctionalInterface
	public interface OnWindowMoved{
		public void onWindowMoved(AWindow window, int windowX, int windowY);
	}
}
