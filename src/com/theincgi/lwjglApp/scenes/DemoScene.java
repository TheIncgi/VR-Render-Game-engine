package com.theincgi.lwjglApp.scenes;

import java.io.File;
import java.util.Optional;

import com.theincgi.lwjglApp.misc.Logger;
import com.theincgi.lwjglApp.mvc.models.Object3D;
import com.theincgi.lwjglApp.mvc.view.drawables.HelloElements;
import com.theincgi.lwjglApp.mvc.view.drawables.HelloElements2;
import com.theincgi.lwjglApp.mvc.view.drawables.HelloTriangle;
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
	Object3D lantern;
	public DemoScene(AWindow window) {
		super(window);
		sceneListener = Optional.of(new SceneCallbackListener());
		Object3D monkey = new Object3D("cmodels/monkey/monkey.obj", 0, 0, -5);
		lantern = new Object3D("cmodels/emissionTest/cube_lamp.obj", 2, 1, -3);
		addDrawables(monkey, lantern);
		for(int x = -4; x<=4; x+=2) {
			for(int y = -4; y<=4; y+=2) {
				final int X = x*2, Y = y*2;
				Object3D cube =  new Object3D("cmodels/softcube/softcube.obj", X, -1, Y);
				addDrawable(cube);
			}
		}
		lantern.getLocation().setYaw(180);
	}
	
	@Override
	public void render(Camera camera, double mouseX, double mouseY) {
		super.render(camera, mouseX, mouseY);
		//lantern.getLocation().rotate(1f, .4f, .7f);
//		cube.ifPresent(c->{
//			//c.getLocation().rotate(.52f, .53334f, .02f);
//			//c.getLocation().setRotation(0,0,0);
//			c.getLocation().setZ(-3);
//			c.getLocation().setY(0);
//		});
		
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
	}
}
