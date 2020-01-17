package com.theincgi.lwjglApp.scenes;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Optional;

import javax.security.auth.callback.CallbackHandler;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.theincgi.lwjglApp.Launcher;
import com.theincgi.lwjglApp.Utils;
import com.theincgi.lwjglApp.misc.Logger;
import com.theincgi.lwjglApp.misc.MatrixStack;
import com.theincgi.lwjglApp.misc.Pair;
import com.theincgi.lwjglApp.misc.RayCast;
import com.theincgi.lwjglApp.misc.Settings;
import com.theincgi.lwjglApp.misc.Tickable;
import com.theincgi.lwjglApp.mvc.models.Object3D;
import com.theincgi.lwjglApp.mvc.models.RadialBounds;
import com.theincgi.lwjglApp.render.Camera;
import com.theincgi.lwjglApp.render.Location;
import com.theincgi.lwjglApp.render.ParticleSystem;
import com.theincgi.lwjglApp.render.Side;
import com.theincgi.lwjglApp.render.TextureManager;
import com.theincgi.lwjglApp.render.animation.Animation;
import com.theincgi.lwjglApp.render.animation.Animation.TimeUnit;
import com.theincgi.lwjglApp.render.text.FontTexture;
import com.theincgi.lwjglApp.render.text.FontTextures;
import com.theincgi.lwjglApp.render.text.TextRenderer;
import com.theincgi.lwjglApp.render.vr.PointingLasers;
import com.theincgi.lwjglApp.render.vr.TouchControllers;
import com.theincgi.lwjglApp.render.vr.VRController;
import com.theincgi.lwjglApp.render.vr.VRController.Input;
import com.theincgi.lwjglApp.render.vr.VRController.Type;
import com.theincgi.lwjglApp.ui.AWindow;
import com.theincgi.lwjglApp.ui.CallbackListener;
import com.theincgi.lwjglApp.ui.Scene;
import com.theincgi.lwjglApp.ui.VRWindow;
import com.theincgi.lwjglApp.vrTests.TestGui;


public class DemoScene extends Scene{
	Object3D lantern;
	Optional<FontTexture> font;
	HashMap<VRController.Input, String> controls;
	Animation testAnimation;
	ParticleSystem testSystem;
	Object3D location;
	private String rayResultMessage = "-";
	private PointingLasers pointingLasers;
	TestGui testGui;
	public DemoScene(AWindow window) {
		super(window);
		sceneListener = Optional.of(new SceneCallbackListener());
		Object3D monkey = new Object3D("cmodels/monkey/monkey.obj", 0, 0, -5);
		monkey.setBounds(new RadialBounds(0, 0, -5, 1));
		monkey.showBounds = true;
		lantern = new Object3D("cmodels/emissionTest/cube_lamp.obj", 2, 1, -3);
		Object3D sky = new Object3D("cmodels/sky/sky_test.obj", "sky");
		location = new Object3D("cmodels/locator/locator.obj", "full");
		addDrawables(monkey, lantern, sky, location);
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
		
		testSystem = new ParticleSystem(this, 0, 1, -5)
		.addForce((psAge, s,d)->{
			if(s.age==0) {
				d.velocity.x =  Utils.ndRandom(.83f);
				d.velocity.y =  Utils.ndRandom(.83f);
				d.velocity.z =-(Utils.ndRandom(.6f)+.43f);
				Vector4f tmp = new Vector4f(d.velocity.x, d.velocity.y, d.velocity.z, 0);
				Matrix4f.transform(new Matrix4f().rotate(-45, new Vector3f(1, 0, 0)), tmp, tmp);
				Matrix4f.transform(Launcher.getMainWindow().vrControllers.getLeftTransform(), tmp,tmp);
				d.velocity.x = tmp.x;
				d.velocity.y = tmp.y;
				d.velocity.z = tmp.z;
				d.velocity.scale(.04f);
			}
			d.emissionStrength = (float)(1-Math.pow(s.age/(float)s.maxAge, 3));
		}).setTexture("particleTextures/star4.png");
		
			addDrawable(pointingLasers = new PointingLasers(Launcher.getMainWindow().vrControllers));
			testSystem.addEmitter(()->{
				Vector4f x = new Vector4f(0,0,0,1);
				Matrix4f.transform(Launcher.getMainWindow().vrControllers.getLeftTransform(), x, x);
				return new Vector3f(x);
			});
		testGui = new TestGui(this);
	}
	
	public void onTick() {
			Vector4f v = Launcher.getMainWindow().vrControllers.getRightPointingVector();
			location.getLocation().pos[0] = v.x;
			location.getLocation().pos[1] = v.y;
			location.getLocation().pos[2] = v.z;
					
			RayCast ray = new RayCast(Launcher.getMainWindow().vrControllers.getLeftPointingSource(), Launcher.getMainWindow().vrControllers.getLeftPointingVector());
			raycast(Launcher.getMainWindow().vrControllers, ray);
			rayResultMessage = ray.result==null? "\n\t低1,0,0;NULL" : 
				"\n\t低0,0,1;"+ray.result.toString()+
				"\n\t低1,1,1;"+"Length: 低0,1,0;"+ray.result.length()+
				"\n\t低1,1,1;Target: 低1,0,1;"+(ray.raycastedObject==null?"unknown object":ray.raycastedObject.toString());
			pointingLasers.setLeftLength(ray.result==null?.05f:ray.result.length());
		 
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
		try(MatrixStack ms = MatrixStack.modelViewStack.push(new Vector3f(-1f, 1, -1.98f))){	
			font.ifPresent(ft->{
					TextRenderer.renderText(ft, 
							 "低1,1,1;Raycast Result: "+rayResultMessage +"\n"
							, true, 8);
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
			if(action == GLFW.GLFW_PRESS)
				Launcher.getMainWindow().showNextMirrorChannel();
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
				testSystem.emit(1000, 8000, 500, 200);
				return true;
			case "tryRaycast":{
				RayCast ray = new RayCast(window.vrControllers.getLeftPointingSource(), window.vrControllers.getLeftPointingVector());
				raycast(window.vrControllers, ray);
				//rayResultMessage = ray.result==null? "低1,0,0;NULL" : "低0,0,1;"+ray.result.toString();
			}
			break;
			case "tryGui":
				testGui.toggle(Side.RIGHT);
				break;
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
		controls.put(TouchControllers.Y_BUTTON, "tryRaycast");
		controls.put(TouchControllers.B_BUTTON, "tryGui");
	}
	
	@Override
	public void onUnload() {
	}
}
