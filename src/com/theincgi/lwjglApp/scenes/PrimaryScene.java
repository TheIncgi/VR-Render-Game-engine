package com.theincgi.lwjglApp.scenes;

import java.util.ArrayList;
import java.util.Optional;

import com.theincgi.lwjglApp.ui.CallbackListener;
import com.theincgi.lwjglApp.ui.Scene;
import com.theincgi.lwjglApp.ui.Window;

public class PrimaryScene extends Scene{
	
	
	public PrimaryScene() {
		sceneListener = Optional.of(new SceneCallbackListener());
		
	}
	
	@Override
	public void render(double mouseX, double mouseY) {
		super.render(mouseX, mouseY);
	}
	
	private class SceneCallbackListener extends CallbackListener
	implements CallbackListener.OnMouseButton {

		@Override
		public boolean onMouseButton(Window window, double x, double y, int button, int action, int mods) {
			System.out.printf("MOUSE BUTTON: Pos: <%6.2f, %6.2f> |Button: button | Window: %s\n", x, y, button, window);
			return true;
		}
		
	}
}
