package com.theincgi.lwjglApp.scenes;

import java.util.Optional;

import com.theincgi.lwjglApp.mvc.view.drawables.HelloTriangle;
import com.theincgi.lwjglApp.render.Camera;
import com.theincgi.lwjglApp.render.Location;
import com.theincgi.lwjglApp.render.shaders.ShaderManager;
import com.theincgi.lwjglApp.render.shaders.ShaderProgram;
import com.theincgi.lwjglApp.ui.CallbackListener;
import com.theincgi.lwjglApp.ui.Scene;
import com.theincgi.lwjglApp.ui.Window;

import static org.lwjgl.opengl.GL45.*;

public class PrimaryScene extends Scene{
	Location sun = new Location(0, 10, 0);
	Camera camera;
	HelloTriangle ht;
	float[] mvpm;
	long startupTime = System.currentTimeMillis();
	
	public PrimaryScene() {
		sceneListener = Optional.of(new SceneCallbackListener());
		camera = new Camera();
		ht = new HelloTriangle();
		mvpm = new float[16];
		
	}
	
	@Override
	public void render(double mouseX, double mouseY) {
		super.render(mouseX, mouseY);
		ShaderManager.forLoaded(s->{
			camera.tellShader(s);
			s.bind();
			s.trySetUniform("uptime", (System.currentTimeMillis()-startupTime)/1000f); //casted to float
			s.trySetUniform("sunPos", sun.pos);
			s.trySetUniform("mode", 4432);
			
		});
		ShaderProgram.unbind();
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
