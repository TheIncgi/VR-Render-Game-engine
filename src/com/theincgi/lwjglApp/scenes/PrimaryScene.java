package com.theincgi.lwjglApp.scenes;

import java.util.Optional;

import com.theincgi.lwjglApp.mvc.view.drawables.HelloTriangle;
import com.theincgi.lwjglApp.render.Camera;
import com.theincgi.lwjglApp.ui.CallbackListener;
import com.theincgi.lwjglApp.ui.Scene;
import com.theincgi.lwjglApp.ui.Window;

import static org.lwjgl.opengl.GL45.*;

public class PrimaryScene extends Scene{
	Camera camera;
	HelloTriangle ht;
	float[] mvpm;
	
	
	public PrimaryScene() {
		sceneListener = Optional.of(new SceneCallbackListener());
		camera = new Camera();
		ht = new HelloTriangle();
		mvpm = new float[16];
		
	}
	
	@Override
	public void render(double mouseX, double mouseY) {
		super.render(mouseX, mouseY);
		//glMatrixMode(GL_PROJECTION);
		//glLoadIdentity();
		//glMatrixMode(GL_MODELVIEW);
		
		
		
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
