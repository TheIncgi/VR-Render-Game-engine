package com.theincgi.lwjglApp.ui;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.openvr.VR.*;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.Optional;

import org.lwjgl.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.openvr.Texture;
import org.lwjgl.openvr.TrackedDevicePose;
import org.lwjgl.openvr.VR;
import org.lwjgl.openvr.VRCompositor;
import org.lwjgl.openvr.VREvent;
import org.lwjgl.openvr.VREventData;
import org.lwjgl.openvr.VREventProperty;
import org.lwjgl.openvr.VRSystem;
import org.lwjgl.system.MemoryStack;

import com.theincgi.lwjglApp.Launcher;
import com.theincgi.lwjglApp.misc.Logger;
import com.theincgi.lwjglApp.misc.MatrixStack;
import com.theincgi.lwjglApp.misc.Pair;
import com.theincgi.lwjglApp.render.EyeCamera;
import com.theincgi.lwjglApp.render.Side;
import com.theincgi.lwjglApp.render.Location;
import com.theincgi.lwjglApp.render.Model;
import com.theincgi.lwjglApp.render.ObjManager;
import com.theincgi.lwjglApp.render.RenderPipeline;
import com.theincgi.lwjglApp.render.RenderPipeline.BufferSet;
import com.theincgi.lwjglApp.render.shaders.ShaderManager;
import com.theincgi.lwjglApp.render.shaders.ShaderProgram;
import com.theincgi.lwjglApp.render.text.TextRenderer;
import com.theincgi.lwjglApp.render.vr.TouchControllers;
import com.theincgi.lwjglApp.render.vr.VRController;
import com.theincgi.lwjglApp.render.vr.VRController.Input;
import com.theincgi.lwjglApp.render.vr.VRController.Type;
import com.theincgi.lwjglApp.render.vr.VRUtil;
import com.theincgi.lwjglApp.ui.CallbackListener.OnJoystick;
import com.theincgi.lwjglApp.ui.CallbackListener.OnVRControllerButtonPress;
import com.theincgi.lwjglApp.ui.CallbackListener.OnVRControllerButtonTouch;
import com.theincgi.lwjglApp.ui.CallbackListener.OnVRControllerButtonUnpress;
import com.theincgi.lwjglApp.ui.CallbackListener.OnVRControllerButtonUntouch;

public class VRWindow extends AWindow{
	EyeCamera leftEye, rightEye;
	public VRController vrControllers;
	/**Looks like each eye is rendered to a texture so additional effects can be applied after rendering. such as chromatic distortion*/

	//private int leftEyeTexture, rightEyeTexture;
	//private int leftDepth, rightDepth;
	//private int leftFrameBuffer, rightFrameBuffer;
	private Model quadMirror;
	private Location quadLocation = new Location(0, 0, 0, 0, 0, -90);
	RenderPipeline renderPipelineLeft, renderPipelineRight;
	private long startTime = System.currentTimeMillis();
	private VRUtil vrUtil;
	TrackedDevicePose.Buffer pRenderPoseArray, pGamePoseArray;
	boolean flipEyes = false; //TODO allow preview to be sterio mode

	public static final String[] renderChannels = new String[] {"renderedTexture","emissionTexture"};
	private int mirrorChannel = 0;
	
	public Optional<ShaderProgram> postShader;
	/**Width and height should now be separate from window resolution*/
	public VRWindow(int wid, int hei, String title, Scene scene) {
		super(wid*2, hei, title, scene);
	}

	@Override
	protected void postInit() {
		int width = vrUtil().getWidth();
		int height = vrUtil().getHeight();
		Logger.preferedLogger.i("VRWindow#postInit", String.format("Creating eye textures at resolution <%d, %d>", width, height));
		leftEye = new EyeCamera();
		rightEye = new EyeCamera().setSide(Side.RIGHT);

//		int internalFormat = GL_RGBA8;
//		int externalFormat = GL_RGBA;
//		int textel         = GL_UNSIGNED_BYTE;
//
//		//http://www.opengl-tutorial.org/intermediate-tutorials/tutorial-14-render-to-texture/
//		leftFrameBuffer = glGenFramebuffers();
//		glBindFramebuffer(GL_FRAMEBUFFER, leftFrameBuffer);
//
//		leftEyeTexture = glGenTextures();
//		glBindTexture(GL_TEXTURE_2D, leftEyeTexture);
//		glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, externalFormat, textel, 0);
//		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
//		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
//		glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP, GL_FALSE);
//		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
//
//
//		leftDepth = glGenRenderbuffers();
//		glBindRenderbuffer(GL_RENDERBUFFER, leftDepth);
//		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
//		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, leftDepth);
//
//		glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, leftEyeTexture, 0);
//
//		glDrawBuffer(GL_COLOR_ATTACHMENT0);
//
//		
//		//and now the right
//		//...
//		rightFrameBuffer = glGenFramebuffers();
//		glBindFramebuffer(GL_FRAMEBUFFER, rightFrameBuffer);
//
//		rightEyeTexture = glGenTextures();
//		glBindTexture(GL_TEXTURE_2D, rightEyeTexture);
//		glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, externalFormat, textel, 0);
//		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
//		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
//		glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP, GL_FALSE);
//		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
//
//		rightDepth = glGenRenderbuffers();
//		glBindRenderbuffer(GL_RENDERBUFFER, rightDepth);
//		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
//		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, rightDepth);
//
//		glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, rightEyeTexture, 0);
//
//		glDrawBuffer(GL_COLOR_ATTACHMENT0);
//
//		//unbinding
//		glBindFramebuffer(GL_FRAMEBUFFER, 0);
//		glBindTexture(GL_TEXTURE_2D, 0);
//		glBindRenderbuffer(GL_RENDERBUFFER, 0);
//
//		
//		
//		//unbinding
//		glBindFramebuffer(GL_FRAMEBUFFER, 0);
//		glBindTexture(GL_TEXTURE_2D, 0);
//		glBindRenderbuffer(GL_RENDERBUFFER, 0);
				
		//quad setup for display
		//quad = ObjManager.INSTANCE.get(new File("cmodels/plane/plane.obj")).get(); //critical
		quadMirror = ObjManager.INSTANCE.get("cmodels/plane/plane.obj", "textureDisplay").get(); //critical
		

		//vrUtil().bindEyeTextures(leftEyeTexture, rightEyeTexture, false);
		//vrUtil().bindEyeBuffers(leftFrameBuffer, rightFrameBuffer, false);
		//pGamePoseArray = TrackedDevicePose.create(VR.k_unMaxTrackedDeviceCount); optional predicted frame
		pRenderPoseArray = TrackedDevicePose.create(VR.k_unMaxTrackedDeviceCount);
		vrControllers = new TouchControllers();
		renderPipelineLeft = new RenderPipeline(vrUtil.getWidth(), vrUtil.getHeight(), renderChannels);
		renderPipelineRight = new RenderPipeline(vrUtil.getWidth(), vrUtil.getHeight(), renderChannels);
//		renderPipelineLeft .appendStep(ShaderManager.INSTANCE.get("blurEmission1").get());
//		renderPipelineRight.appendStep(ShaderManager.INSTANCE.get("blurEmission1").get());
//		
//		renderPipelineLeft .appendStep(ShaderManager.INSTANCE.get("blurEmission1").get());
//		renderPipelineRight.appendStep(ShaderManager.INSTANCE.get("blurEmission1").get());
//		
		renderPipelineLeft .appendStep(ShaderManager.INSTANCE.get("blurEmission1").get());
		renderPipelineRight.appendStep(ShaderManager.INSTANCE.get("blurEmission1").get());
//		
		renderPipelineLeft .appendStep(ShaderManager.INSTANCE.get("blurEmission1").get());
		renderPipelineRight.appendStep(ShaderManager.INSTANCE.get("blurEmission1").get());
		
		renderPipelineLeft .appendStep(ShaderManager.INSTANCE.get("blurEmission1").get());
		renderPipelineRight.appendStep(ShaderManager.INSTANCE.get("blurEmission1").get());
//		
		renderPipelineLeft .appendStep(ShaderManager.INSTANCE.get("blurEmission1").get());
		renderPipelineRight.appendStep(ShaderManager.INSTANCE.get("blurEmission1").get());
		renderPipelineLeft .appendStep(ShaderManager.INSTANCE.get("post").get());
		renderPipelineRight.appendStep(ShaderManager.INSTANCE.get("post").get());
		TextRenderer.init();
	}

	protected void cleanup() {
		//pGamePoseArray.free();
		pRenderPoseArray.free();
	}


	@Override
	void loop() {
		while ( !glfwWindowShouldClose(WINDOW_HANDLE) ) {

			scene.ifPresentOrElse(value->{
				value.onTick(); //TODO move to logic thread
				_render(value);

			}, /*else*/()->{
				setViewport(0, 0, width, height);
				glClearColor(0,0,0,0);
				glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			});
			// clear the framebuffer

			glfwSwapBuffers(WINDOW_HANDLE); // swap the color buffers

			glfwPollEvents();
		}
		cleanup();
	}


	private void _render(Scene value) {
		glEnable(GL_DEPTH_TEST);
		setViewport(0, 0, width, height);
		Color cc = value.clearColor;
		glClearColor(cc.r(), cc.g(), cc.b(), cc.a());
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);


		VRCompositor.VRCompositor_WaitGetPoses(pRenderPoseArray, null);
		TrackedDevicePose hmdPose = pRenderPoseArray.get(VR.k_unTrackedDeviceIndex_Hmd);
		
		
		
		
		try(MemoryStack ms = MemoryStack.stackPush()){
			IntBuffer err = ms.mallocInt(1);
			FloatBuffer sinceLastVsyncBuff = ms.mallocFloat(1);
			LongBuffer frames = ms.mallocLong(1);
			
			for (int i = 1; i < pRenderPoseArray.limit(); i++) {
				TrackedDevicePose tdp = pRenderPoseArray.get(i);
				if(tdp == null || !tdp.bDeviceIsConnected() || !tdp.bPoseIsValid()) continue;
				String name = VRSystem.VRSystem_GetStringTrackedDeviceProperty(i, ETrackedDeviceProperty_Prop_RenderModelName_String, err);
				switch(name) {
					case "oculus_cv1_controller_left":
						vrControllers.updatePoseLeft(i, tdp);
						break;
					case "oculus_cv1_controller_right":
						vrControllers.updatePoseRight(i, tdp);
						break;
					case "rift_camera":
						break;
					default:
						Logger.preferedLogger.w("VRWindow#_render", "Unhandled device: "+name);
				}
			}
			
//			VRSystem.VRSystem_GetTimeSinceLastVsync(sinceLastVsyncBuff, frames);
//			float fSecondsSinceLastVsync = sinceLastVsyncBuff.get(0);
//			float fDisplayFrequency = VRSystem.VRSystem_GetFloatTrackedDeviceProperty(VR.k_unTrackedDeviceIndex_Hmd, VR.ETrackedDeviceProperty_Prop_DisplayFrequency_Float, err);
//			float fFrameDuration = 1f / fDisplayFrequency;
//			float fVsyncToPhotons = VRSystem.VRSystem_GetFloatTrackedDeviceProperty(VR.k_unTrackedDeviceIndex_Hmd, VR.ETrackedDeviceProperty_Prop_SecondsFromVsyncToPhotons_Float, err);
//			float fPredictedSecondsFromNow = fFrameDuration - fSecondsSinceLastVsync + fVsyncToPhotons;
//			VRSystem.nVRSystem_GetDeviceToAbsoluteTrackingPose(ETrackingUniverseOrigin_TrackingUniverseStanding, fPredictedSecondsFromNow, pRenderPoseArray.address(), VR.k_unMaxTrackedDeviceCount);
//			hmdPose = pRenderPoseArray.get(VR.k_unTrackedDeviceIndex_Hmd);
		}


		leftEye.setHmdPose(hmdPose);
		rightEye.setHmdPose(hmdPose);
		
		
		
		
		
		
		//left eye
		renderPipelineLeft.bind();
		setViewport(0, 0, vrUtil().getWidth(), vrUtil().getHeight());
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); //Disabling this makes fun effects		
		value.render(flipEyes?rightEye:leftEye, -1, -1);
		BufferSet left = renderPipelineLeft.process();
				
		//right eye
		renderPipelineRight.bind();
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); //Disabling this makes fun effects
		value.render(flipEyes?leftEye:rightEye, -1, -1);

		BufferSet right = renderPipelineRight.process();
		vrUtil().submitFrame(left.output, right.output, VR.EVRSubmitFlags_Submit_Default);
		
		
		
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		setViewport(0, 0, width/2, height);
		setShaderUniforms(left.textureChannels[mirrorChannel]);
		quadMirror.drawAt(quadLocation);

		setViewport(width/2, 0, width/2, height);
		setShaderUniforms(right.textureChannels[mirrorChannel]);
		quadMirror.drawAt(quadLocation);
		
		
		GL11.glFlush();

		VREvent vrEvent = VREvent.create();

		// Process SteamVR events
		while (VRSystem.VRSystem_PollNextEvent(vrEvent)) {
			processVREvent(vrEvent);
		}
	}


	private void processVREvent(VREvent vrEvent) {
		System.out.println("Event type: "+ VRUtil.getEventName(vrEvent.eventType()));
		int device = vrEvent.trackedDeviceIndex();
		VREventData eventData = vrEvent.data();
		
		switch (vrEvent.eventType()) {
		case EVREventType_VREvent_ButtonPress:{
			Pair<Type, Side> cntrl = getControllerTypeSide(device);
			if(cntrl==null)break;
			for (CallbackListener callbackListener : callbackListeners) {
				if(callbackListener instanceof OnVRControllerButtonPress)
					if(((OnVRControllerButtonPress)callbackListener).onPress(this, new Input(cntrl.x, cntrl.y, false, eventData.controller().button()))) return;
			}
			break;
		}
		case EVREventType_VREvent_ButtonTouch:{
			Pair<Type, Side> cntrl = getControllerTypeSide(device);
			if(cntrl==null)break;
			for (CallbackListener callbackListener : callbackListeners) {
				if(callbackListener instanceof OnVRControllerButtonTouch)
					if(((OnVRControllerButtonTouch)callbackListener).onTouch(this, new Input(cntrl.x, cntrl.y, false, eventData.controller().button()))) return;
			}
			break;
		}
		case EVREventType_VREvent_ButtonUnpress:{
			Pair<Type, Side> cntrl = getControllerTypeSide(device);
			if(cntrl==null)break;
			for (CallbackListener callbackListener : callbackListeners) {
				if(callbackListener instanceof OnVRControllerButtonUnpress)
					if(((OnVRControllerButtonUnpress)callbackListener).onUnpress(this, new Input(cntrl.x, cntrl.y, false, eventData.controller().button()))) return;
			}
			break;
		}
		case EVREventType_VREvent_ButtonUntouch:{
			Pair<Type, Side> cntrl = getControllerTypeSide(device);
			if(cntrl==null)break;
			for (CallbackListener callbackListener : callbackListeners) {
				if(callbackListener instanceof OnVRControllerButtonUntouch)
					if(((OnVRControllerButtonUntouch)callbackListener).onUntouch(this, new Input(cntrl.x, cntrl.y, false, eventData.controller().button()))) return;
			}
			break;
		}
		case EVREventType_VREvent_EnterStandbyMode:
		case EVREventType_VREvent_LeaveStandbyMode:
		case EVREventType_VREvent_TrackedDeviceUserInteractionStarted:
		case EVREventType_VREvent_TrackedDeviceUserInteractionEnded:
			break;
		case EVREventType_VREvent_PropertyChanged:
			VREventProperty propEvent = eventData.property();
			break;

		default:
			break;
		}

	}
	
	private Pair<VRController.Type, Side> getControllerTypeSide(int device){
		VRController.Type type = null;
		Side side = null;
		try(MemoryStack ms = MemoryStack.stackPush()){
			IntBuffer err = ms.mallocInt(1);
			String name = VRSystem.VRSystem_GetStringTrackedDeviceProperty(device, ETrackedDeviceProperty_Prop_RenderModelName_String, err);
			switch(name) {
			case "oculus_cv1_controller_left":
				side = Side.LEFT; type = VRController.Type.OCULUS_TOUCH;
				break;
			case "oculus_cv1_controller_right":
				side = Side.RIGHT; type = VRController.Type.OCULUS_TOUCH;
				break;
			default:
				Logger.preferedLogger.w("VRWindow#getDevice", "Unhandled device: "+name);
			}
		}
		if(type==null || side==null) return null;
		return new Pair<>(type, side);
	}
	
	private void setShaderUniforms(final int texture) {
		quadMirror.shader.ifPresent(s->{
			s.bind();
			s.trySetUniform("uptime", (System.currentTimeMillis()-startTime)/1000f); //casted to float
			s.trySetUniformTexture("renderedTexture", texture, 0);
			ShaderProgram.unbind();
		});
		glEnable(GL_DEPTH_TEST);
	}

	@Override
	public void setScene(Scene s) {
		super.setScene(null);
		startTime = System.currentTimeMillis(); //refresh timer to help prevent strangeness at higher values
		super.setScene(s);
	}

	
	
	
	private VRUtil vrUtil() {
		if(vrUtil==null) vrUtil = Launcher.getVrUtil();
		return vrUtil;
	}
	
	@Override
	public Optional<Window> getWindow() {
		return Optional.empty();
	}
	@Override
	public Optional<VRWindow> getVRWindow() {
		return Optional.of(this);
	}
	@Override
	public boolean isVR() {
		return true;
	}
	
	public void showNextMirrorChannel() {
		mirrorChannel = (mirrorChannel + 1) % renderChannels.length;
	}
}
