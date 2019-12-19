package com.theincgi.lwjglApp.scenes;

import java.util.ArrayList;
import java.util.Optional;

import com.theincgi.lwjglApp.mvc.view.drawables.HelloTriangle;
import com.theincgi.lwjglApp.ui.CallbackListener;
import com.theincgi.lwjglApp.ui.Scene;
import com.theincgi.lwjglApp.ui.Window;

public class PrimaryScene extends Scene{
	
	HelloTriangle ht;
	float[] mvpm;
	public PrimaryScene() {
		sceneListener = Optional.of(new SceneCallbackListener());
		ht = new HelloTriangle();
		mvpm = new float[16];
		
	}
	
	@Override
	public void render(double mouseX, double mouseY) {
		super.render(mouseX, mouseY);
		ht.draw(mvpm);
	}
	
	private class SceneCallbackListener extends CallbackListener
	implements CallbackListener.OnMouseButton {

		@Override
		public boolean onMouseButton(Window window, double x, double y, int button, int action, int mods) {
			System.out.printf("MOUSE BUTTON: Pos: <%6.2f, %6.2f> |Button: button | Window: %s\n", x, y, button, window);
			return true;
		}
		
	}
	
	
	@Override
	public void onUnload() {
		ht.onDestroy();
	}
}
