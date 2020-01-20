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
import com.theincgi.lwjglApp.mvc.models.AABB;
import com.theincgi.lwjglApp.mvc.models.Colideable;
import com.theincgi.lwjglApp.mvc.models.Object3D;
import com.theincgi.lwjglApp.mvc.models.RadialBounds;
import com.theincgi.lwjglApp.mvc.models.SolidObject;
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
	private String rayResultMessageLeft = "-";
	private String rayResultMessageRight = "-";
	private PointingLasers pointingLasers;
	TestGui testGui;
	//AABB testAABB = new AABB(-1.5f, 2, -3, -.5f, 3, -2);
	private RayCast lastRayLeft, lastRayRight;
	public DemoScene(AWindow window) {
		super(window);
		sceneListener = Optional.of(new SceneCallbackListener());
		SolidObject monkey = new SolidObject("cmodels/monkey/monkey.obj", new AABB( -1, -1, -6, 1, 1, -4), 0, 0, -5);
		//monkey.setBounds(new RadialBounds(0, 0, -5, 1));
		lantern = new Object3D("cmodels/emissionTest/cube_lamp.obj", 3, 1, -1);
		Object3D sky = new Object3D("cmodels/sky/sky_test.obj", "sky");
		location = new Object3D("cmodels/locator/locator.obj", "full");
		addDrawables(monkey, lantern, sky, location);
		for(int x = -4; x<=4; x+=2) {
			for(int y = -4; y<=4; y+=2) {
				if(x==0 && y==-2) continue;
				final int X = x*2, Y = y*2;
				Object3D cube =  new Object3D("cmodels/softcube/softcube.obj", X, -1, Y);
				addDrawable(cube);
				//				cube.setBounds(new AABB(X-1, -2, Y-1, X+1, 0, Y+1));
				cube.setLabel("Ground cube <"+X+":"+Y+">");
			}
		}
		lantern.getLocation().setYaw(180);
		font = FontTextures.INSTANCE.get(new Pair<>("ascii_consolas", 100));
		createDefaultDebugControls();

		testAnimation = new Animation(this, Animation.Updater.makeFloatUpdater(Animation.Interpolator.SIGMOID, 0f, 90f, v->{
			monkey.getLocation().setYaw(v);
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
		if(lastRayLeft!=null && lastRayLeft.raycastedBounds.isPresent()){
			lastRayLeft.raycastedBounds.get().getParent().setShowBounds(false);
		}if(lastRayRight!=null && lastRayRight.raycastedBounds.isPresent()){
			lastRayRight.raycastedBounds.get().getParent().setShowBounds(false);
		}
		lastRayLeft = new RayCast(Launcher.getMainWindow().vrControllers.getLeftPointingSource(), Launcher.getMainWindow().vrControllers.getLeftPointingVector());
		raycast(Launcher.getMainWindow().vrControllers, lastRayLeft);
		lastRayRight= new RayCast(Launcher.getMainWindow().vrControllers.getRightPointingSource(), Launcher.getMainWindow().vrControllers.getRightPointingVector());
		raycast(Launcher.getMainWindow().vrControllers, lastRayRight);

		
		if(lastRayRight.raycastedBounds!=null) lastRayRight.raycastedBounds.ifPresent(b->b.getParent().setShowBounds(true));

		lastRayLeft.result.ifPresentOrElse(result->{
			lastRayLeft.raycastedBounds.ifPresent(b->b.getParent().setShowBounds(true));
			rayResultMessageLeft =
					"\n\t低0,0,1;"+result.toString()+
					"\n\t低1,1,1;"+"Length: 低0,1,0;"+result.length()+
					"\n\t低1,1,1;Target: 低1,0,1;"+(lastRayLeft.raycastedBounds.isEmpty()?"unknown object":lastRayLeft.raycastedBounds.get().toString());
			pointingLasers.setLeftLength(result.length());
		}, ()->{
			rayResultMessageLeft = "\n\t低1,0,0;NULL";
			pointingLasers.setLeftLength(.05f);
		});


		lastRayRight.result.ifPresentOrElse(result->{
			rayResultMessageRight = 
					"\n\t低0,0,1;"+result.toString()+
					"\n\t低1,1,1;"+"Length: 低0,1,0;"+result.length()+
					"\n\t低1,1,1;Target: 低1,0,1;"+(lastRayRight.raycastedBounds.isEmpty()?"unknown object":lastRayRight.raycastedBounds.get().toString());
			pointingLasers.setRightLength(result.length());
		}, ()->{
			rayResultMessageRight = "\n\t低1,0,0;NULL";
			pointingLasers.setRightLength(.05f);
		});


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
						"低1,1,1;Raycast Result [left ]: "+rayResultMessageLeft +"\n"+
								"低1,1,1;Raycast Result [right]: "+rayResultMessageRight
								, true, 8);
			});
		}
		RayCast test = new RayCast(Launcher.getMainWindow().vrControllers.getLeftPointingSource(), Launcher.getMainWindow().vrControllers.getLeftPointingVector());

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
		controls.put(TouchControllers.B_BUTTON, "tryGui");
	}

	@Override
	public void onUnload() {
	}
}
