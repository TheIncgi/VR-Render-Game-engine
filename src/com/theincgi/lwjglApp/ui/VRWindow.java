package com.theincgi.lwjglApp.ui;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.openvr.VR.*;

import java.io.File;

import org.lwjgl.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.openvr.OpenVR.IVRCompositor;
import org.lwjgl.openvr.TrackedDevicePose;
import org.lwjgl.openvr.VR;
import org.lwjgl.openvr.VRCompositor;
import org.lwjgl.openvr.VREvent;
import org.lwjgl.openvr.VREventData;
import org.lwjgl.openvr.VREventProperty;
import org.lwjgl.openvr.VRSystem;
import org.lwjgl.util.vector.Matrix;

import com.theincgi.lwjglApp.Launcher;
import com.theincgi.lwjglApp.misc.Logger;
import com.theincgi.lwjglApp.misc.MatrixStack;
import com.theincgi.lwjglApp.misc.Pair;
import com.theincgi.lwjglApp.mvc.view.drawables.Quad;
import com.theincgi.lwjglApp.render.Camera;
import com.theincgi.lwjglApp.render.EyeCamera;
import com.theincgi.lwjglApp.render.EyeSide;
import com.theincgi.lwjglApp.render.Model;
import com.theincgi.lwjglApp.render.ObjManager;
import com.theincgi.lwjglApp.render.shaders.ShaderManager;
import com.theincgi.lwjglApp.render.shaders.ShaderProgram;
import com.theincgi.lwjglApp.render.vr.VRUtil;

public class VRWindow extends AWindow{
	EyeCamera leftEye, rightEye;
	/**Looks like each eye is rendered to a texture so additional effects can be applied after rendering. such as chromatic distortion*/
	
	private int leftEyeTexture, rightEyeTexture;
	private int leftDepth, rightDepth;
	private int leftFrameBuffer, rightFrameBuffer;
	private Model quad;
	private long startTime = System.currentTimeMillis();
	private VRUtil vrUtil;
	TrackedDevicePose.Buffer pRenderPoseArray, pGamePoseArray;
	
	
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
		rightEye = new EyeCamera().setSide(EyeSide.RIGHT);
		
		int internalFormat = GL_RGBA8;
		int externalFormat = GL_RGBA;
		//RED GREEN BLUE ALPHA RG RGB RGBA BGR 
		//BGRA RED_INTEGER GREEN_INTEGER BLUE_INTEGER ALPHA_INTEGER RG_INTEGER RGB_INTEGER RGBA_INTEGER 
		//BGR_INTEGER BGRA_INTEGER STENCIL_INDEX DEPTH_COMPONENT DEPTH_STENCIL 

		int textel         = GL_UNSIGNED_BYTE;
		
		//http://www.opengl-tutorial.org/intermediate-tutorials/tutorial-14-render-to-texture/
		leftFrameBuffer = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, leftFrameBuffer);
		
		leftEyeTexture = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, leftEyeTexture);
		glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, externalFormat, textel, 0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP, GL_FALSE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
		
		
		leftDepth = glGenRenderbuffers();
		glBindRenderbuffer(GL_RENDERBUFFER, leftDepth);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, leftDepth);
		
		glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, leftEyeTexture, 0);
		
		glDrawBuffer(GL_COLOR_ATTACHMENT0);
		
		//and now the right
		//...
		rightFrameBuffer = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, rightFrameBuffer);
		
		rightEyeTexture = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, rightEyeTexture);
		glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, externalFormat, textel, 0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP, GL_FALSE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
		
		rightDepth = glGenRenderbuffers();
		glBindRenderbuffer(GL_RENDERBUFFER, rightDepth);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, rightDepth);
		
		glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, rightEyeTexture, 0);
		
		glDrawBuffer(GL_COLOR_ATTACHMENT0);
		
		//unbinding
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glBindTexture(GL_TEXTURE_2D, 0);
		glBindRenderbuffer(GL_RENDERBUFFER, 0);
		
		
		//quad setup for display
		//quad = ObjManager.INSTANCE.get(new File("cmodels/plane/plane.obj")).get(); //critical
		quad = ObjManager.INSTANCE.get(new File("cmodels/plane/plane.obj")).get(); //critical
		quad.shader = ShaderManager.INSTANCE.get("textureDisplay");
		
		vrUtil().bindEyeTextures(leftEyeTexture, rightEyeTexture, false);
		pGamePoseArray = TrackedDevicePose.create(VR.k_unMaxTrackedDeviceCount);
		pRenderPoseArray = TrackedDevicePose.create(VR.k_unMaxTrackedDeviceCount);
	}
	
	protected void cleanup() {
		quad.onDestroy();
		pGamePoseArray.free();
		pRenderPoseArray.free();
	}
	
	
	@Override
	void loop() {
		while ( !glfwWindowShouldClose(WINDOW_HANDLE) ) {
			
			scene.ifPresentOrElse(value->{
				
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
		
		
		VRCompositor.VRCompositor_WaitGetPoses(pRenderPoseArray, pGamePoseArray);
		for (int i = 0; i < pRenderPoseArray.limit(); i++) {
			pRenderPoseArray.get(i).
		}
		//left eye
		glBindFramebuffer(GL_FRAMEBUFFER, leftFrameBuffer);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); //Disabling this makes fun effects
		setViewport(0, 0, vrUtil().getWidth(), vrUtil().getHeight());
		value.render(leftEye, -1, -1);
		
		//right eye
		glBindFramebuffer(GL_FRAMEBUFFER, rightFrameBuffer);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); //Disabling this makes fun effects
		//setViewport(0, 0, vrUtil().getWidth(), vrUtil().getHeight()); still set
		value.render(rightEye, -1, -1);
		
		
		vrUtil().submitFrame();
		
		
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		setViewport(0, 0, width/2, height);
		setShaderUniforms(leftEyeTexture);
		quad.draw();
		
		setViewport(width/2, 0, width/2, height);
		setShaderUniforms(rightEyeTexture);
		quad.draw();
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
		
		case EVREventType_VREvent_ButtonPress:
		case EVREventType_VREvent_ButtonTouch:
		case EVREventType_VREvent_ButtonUnpress:
		case EVREventType_VREvent_ButtonUntouch:
			break;
		case EVREventType_VREvent_PropertyChanged:
			VREventProperty propEvent = eventData.property();
			break;

		default:
			break;
		}
		
	}

	private void setShaderUniforms(final int texture) {
		quad.shader.ifPresent(s->{
			s.bind();
			leftEye.tellShader(s);
			s.trySetUniform("uptime", (System.currentTimeMillis()-startTime)/1000f); //casted to float
			s.trySetUniformTexture("renderedTexture", texture, 0);
			MatrixStack.projection.reset();
			s.trySetMatrix("projectionMatrix", MatrixStack.projection.get());
			s.trySetUniform("sunColor", Color.WHITE.vec());
			s.trySetUniform("sunPos", rightEye.getLocation().pos);
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
	
	void fullscreenTexture(int textureID) {
		
		quad.draw();
	}
}
