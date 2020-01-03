package com.theincgi.lwjglApp.scenes;

import java.io.File;
import java.util.Optional;

import com.theincgi.lwjglApp.misc.Logger;
import com.theincgi.lwjglApp.render.Camera;
import com.theincgi.lwjglApp.render.Drawable;
import com.theincgi.lwjglApp.render.EyeCamera;
import com.theincgi.lwjglApp.render.Model;
import com.theincgi.lwjglApp.render.ObjManager;
import com.theincgi.lwjglApp.render.shaders.ShaderManager;
import com.theincgi.lwjglApp.ui.AWindow;
import com.theincgi.lwjglApp.ui.CallbackListener;
import com.theincgi.lwjglApp.ui.Scene;


public class DemoScene extends Scene{
	
	Drawable ht;
	Optional<Model> cube;
	
	
	public DemoScene(AWindow window) {
		super(window);
		sceneListener = Optional.of(new SceneCallbackListener());
		cube = ObjManager.INSTANCE.get(new File("cmodels/monkey/monkey.obj"));
		cube.ifPresent(c->{
			c.shader = ShaderManager.INSTANCE.get("basic");
			Logger.preferedLogger.i("PrimaryScene", "Cube loaded");
			addDrawable(c);
		});
	}
	
	@Override
	public void render(Camera camera, double mouseX, double mouseY) {
		super.render(camera, mouseX, mouseY);
		
		cube.ifPresent(c->{
			//c.getLocation().rotate(.0052f, .0053334f, .0002f);
			//c.getLocation().setRotation(0,0,90);
			c.getLocation().setZ(-3);
			c.getLocation().setY(0);
		});
		
	}
	
	private class SceneCallbackListener extends CallbackListener
	implements CallbackListener.OnMouseButton {

		@Override
		public boolean onMouseButton(AWindow window, double x, double y, int button, int action, int mods) {
			System.out.printf("MOUSE BUTTON: Pos: <%6.2f, %6.2f> |Button: button | Window: %s\n", x, y, button, window);
			return true;
		}
		
	}
	
	
	@Override
	public void onUnload() {
		ht.onDestroy();
	}
}
