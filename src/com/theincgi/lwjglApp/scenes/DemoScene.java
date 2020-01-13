package com.theincgi.lwjglApp.scenes;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Optional;

import javax.security.auth.callback.CallbackHandler;

import org.lwjgl.util.vector.Vector3f;

import com.theincgi.lwjglApp.misc.Logger;
import com.theincgi.lwjglApp.misc.MatrixStack;
import com.theincgi.lwjglApp.misc.Pair;
import com.theincgi.lwjglApp.misc.Settings;
import com.theincgi.lwjglApp.misc.Tickable;
import com.theincgi.lwjglApp.mvc.models.Object3D;
import com.theincgi.lwjglApp.render.Camera;
import com.theincgi.lwjglApp.render.Location;
import com.theincgi.lwjglApp.render.ParticleSystem;
import com.theincgi.lwjglApp.render.Side;
import com.theincgi.lwjglApp.render.animation.Animation;
import com.theincgi.lwjglApp.render.animation.Animation.TimeUnit;
import com.theincgi.lwjglApp.render.text.FontTexture;
import com.theincgi.lwjglApp.render.text.FontTextures;
import com.theincgi.lwjglApp.render.text.TextRenderer;
import com.theincgi.lwjglApp.render.vr.TouchControllers;
import com.theincgi.lwjglApp.render.vr.VRController;
import com.theincgi.lwjglApp.render.vr.VRController.Input;
import com.theincgi.lwjglApp.render.vr.VRController.Type;
import com.theincgi.lwjglApp.ui.AWindow;
import com.theincgi.lwjglApp.ui.CallbackListener;
import com.theincgi.lwjglApp.ui.Scene;
import com.theincgi.lwjglApp.ui.VRWindow;


public class DemoScene extends Scene{
	Object3D lantern;
	Optional<FontTexture> font;
	HashMap<VRController.Input, String> controls;
	Animation testAnimation;
	ParticleSystem testSystem;
	public DemoScene(AWindow window) {
		super(window);
		sceneListener = Optional.of(new SceneCallbackListener());
		Object3D monkey = new Object3D("cmodels/monkey/monkey.obj", 0, 0, -5);
		lantern = new Object3D("cmodels/emissionTest/cube_lamp.obj", 2, 1, -3);
		Object3D sky = new Object3D("cmodels/sky/sky_test.obj", "sky");
		sky.setShader("sky");
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
		createDefaultDebugControls();
		
		testAnimation = new Animation(this, Animation.Updater.makeFloatUpdater(Animation.Interpolator.SIGMOID, 0f, 90f, v->{
			monkey.getLocation().setYaw(v);
			System.out.println("\t"+v);
		})).setDuration(5f, TimeUnit.SECONDS);
		addTickable(testAnimation);
		
		testSystem = new ParticleSystem(this, 0, 1, -5);
		testSystem.addForce((s,d)->{
			if(s.age==0) {
				d.velocity.x = (float) (Math.random()*.1);
				d.velocity.y = (float) (Math.random()*.3);
				d.velocity.z = (float) (Math.random()*.1);
			}
		});
	}
	
	public void onTick() {
		synchronized (tickables) {
			LinkedList<Tickable> toRemove = new LinkedList<>();
			tickables.forEach(t->{
				if(t.onTickUpdate())
					toRemove.add(t);
			});
			while(!toRemove.isEmpty())
				tickables.remove(toRemove.removeFirst());
		}
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
	implements CallbackListener.OnMouseButton, CallbackListener.OnVRControllerButtonPress, CallbackListener.OnVRControllerButtonUnpress, CallbackListener.OnVRControllerButtonTouch, CallbackListener.OnVRControllerButtonUntouch {

		@Override
		public boolean onMouseButton(AWindow window, double x, double y, int button, int action, int mods) {
			System.out.printf("MOUSE BUTTON: Pos: <%6.2f, %6.2f> |Button: button | Window: %s\n", x, y, button, window);
			return true;
		}

		@Override public boolean onTouch(VRWindow window, VRController.Input input) {
			Logger.preferedLogger.d("DemoScene#onPress", "PRESSED: "+input);
			String action = controls.get(input);
			if(action==null) return false;
			switch (action) {
			case "playAnimationTest": //should be constants for a proper scene
				Logger.preferedLogger.i("DemoScene#onPress", "Playing an animation...");
				testAnimation.playToggled();
				return true;
			
			default:
				break;
			}
			return false;
		}
		
		@Override public boolean onUntouch(VRWindow window, Input input) {
			String action = controls.get(input);
			if(action==null) return false;
			switch (action) {
			case "playAnimationTest": //should be constants for a proper scene
				testAnimation.pause();
				return true;

			default:
				break;
			}
			return false;
		}
		
		@Override
		public boolean onPress(VRWindow window, Input input) {
			String action = controls.get(input);
			if(action==null) return false;
			switch (action) {
			case "tryParticleSystem":
				testSystem.emit(1000, 1000, 500, 200);
				return true;
				
			default:
				break;
			}
			return false;
		}
		@Override
		public boolean onUnpress(VRWindow window, Input input) {
			return false;
		}
	}
	
	public void createDefaultDebugControls() {
		controls = Settings.computeControllMappingIfAbsent(Settings.CONTROLS+"debug");
		//TODO bind some buttons
		//1 y or b
		//7 x or a
		//32 thumb stick push
		//33 trigger
		//2 or 34 but 2 fires first
		controls.put(TouchControllers.A_BUTTON, "playAnimationTest");
		controls.put(TouchControllers.X_BUTTON, "tryParticleSystem");
	}
	
	@Override
	public void onUnload() {
	}
}
