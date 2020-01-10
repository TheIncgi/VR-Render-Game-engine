package com.theincgi.lwjglApp.scenes;

import java.io.File;
import java.util.Optional;

import org.lwjgl.util.vector.Vector3f;

import com.theincgi.lwjglApp.Launcher;
import com.theincgi.lwjglApp.misc.Logger;
import com.theincgi.lwjglApp.misc.MatrixStack;
import com.theincgi.lwjglApp.misc.Pair;
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
import com.theincgi.lwjglApp.render.text.FontTexture;
import com.theincgi.lwjglApp.render.text.FontTextures;
import com.theincgi.lwjglApp.render.text.TextRenderer;
import com.theincgi.lwjglApp.render.vr.TouchControllers;
import com.theincgi.lwjglApp.ui.AWindow;
import com.theincgi.lwjglApp.ui.CallbackListener;
import com.theincgi.lwjglApp.ui.Scene;
import com.theincgi.lwjglApp.ui.VRWindow;


public class DemoScene extends Scene{
	Object3D lantern;
	Optional<FontTexture> font;
	public DemoScene(AWindow window) {
		super(window);
		sceneListener = Optional.of(new SceneCallbackListener());
		Object3D monkey = new Object3D("cmodels/monkey/monkey.obj", 0, 0, -5)
		{
			@Override
			public void draw() {
				if(Launcher.getMainWindow() instanceof VRWindow) {
					VRWindow w = (VRWindow)Launcher.getMainWindow();
					if(w.vrControllers instanceof TouchControllers)
						if(!((TouchControllers)w.vrControllers).isBPressed())
							super.draw();
				}
				//super.draw();
			}
		};
		lantern = new Object3D("cmodels/emissionTest/cube_lamp.obj", 2, 1, -3);
		Object3D sky = new Object3D("cmodels/sky/sky_test.obj") {
			@Override
			public void draw() {
				if(Launcher.getMainWindow() instanceof VRWindow) {
					VRWindow w = (VRWindow)Launcher.getMainWindow();
					if(w.vrControllers instanceof TouchControllers)
						if(!((TouchControllers)w.vrControllers).isAPressed())
							super.draw();
				}
				//super.draw();
			}
		}; sky.setShader("sky");
		addDrawables(monkey, lantern, sky);
		for(int x = -4; x<=4; x+=2) {
			for(int y = -4; y<=4; y+=2) {
				final int X = x*2, Y = y*2;
				Object3D cube =  new Object3D("cmodels/softcube/softcube.obj", X, -1, Y);
				addDrawable(cube);
			}
		}
		lantern.getLocation().setYaw(180);
		font = FontTextures.INSTANCE.get(new Pair<>("ascii_consolas", 100));
	}
	
	@Override
	public void render(Camera camera, double mouseX, double mouseY) {
		super.render(camera, mouseX, mouseY);
		try(MatrixStack ms = MatrixStack.modelViewStack.pushTranslate(new Vector3f(0, 2, -3f))){	
			font.ifPresent(ft->{
					TextRenderer.renderText(ft, "Test\nWorld\n低1,0,0;Ok\n低0,1,0;伯Bold 呆plain\n"
							+ "低0,0,1;兌Italics告 normal\n"
							+ "低1,1,0;刨Strke吱thru\n"
							+ "低1,1,0;刨Strke吱thru\n"
							+ "低1,1,0;刨Strke吱thru\n"
							+ "低1,1,0;刨Strke吱thru\n"
							+ "低1,1,0;刨Strke吱thru\n"
							+ "低1,1,0;刨Strke吱thru\n"
							+ "低1,1,0;刨Strke吱thru\n"
							+ "低1,1,0;刨Strke吱thru\n"
							+ "低1,1,0;刨Strke吱thru\n"
							+ "低1,0,1;助Under吟line", true, 8);
			});
		}
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
