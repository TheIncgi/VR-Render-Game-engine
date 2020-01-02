package com.theincgi.lwjglApp.ui;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL45.*;

import java.io.File;

import org.lwjgl.*;
import org.lwjgl.util.vector.Matrix;

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

public class VRWindow extends AWindow{
	EyeCamera leftEye, rightEye;
	/**Looks like each eye is rendered to a texture so additional effects can be applied after rendering. such as chromatic distortion*/
	
	private int leftEyeTexture, rightEyeTexture;
	private int leftDepth, rightDepth;
	private int leftFrameBuffer, rightFrameBuffer;
	private Model quad;
	private long startTime = System.currentTimeMillis();
	
	public VRWindow(int wid, int hei, String title, Scene scene) {
		super(wid*2, hei, title, scene);
	}
	
	@Override
	protected void postInit() {
		leftEye = new EyeCamera();
		rightEye = new EyeCamera().setSide(EyeSide.RIGHT);
		
		//http://www.opengl-tutorial.org/intermediate-tutorials/tutorial-14-render-to-texture/
		leftFrameBuffer = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, leftFrameBuffer);
		
		leftEyeTexture = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, leftEyeTexture);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, 0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		
		leftDepth = glGenRenderbuffers();
		glBindRenderbuffer(GL_RENDERBUFFER, leftDepth);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, leftDepth);
		
		glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, leftEyeTexture, 0);
		
		glDrawBuffer(GL_COLOR_ATTACHMENT0);
		
		//and now the right
		//...
		
		
		
		//quad setup for display
		//quad = ObjManager.INSTANCE.get(new File("cmodels/plane/plane.obj")).get(); //critical
		quad = ObjManager.INSTANCE.get(new File("cmodels/plane/plane.obj")).get(); //critical
		quad.shader = ShaderManager.INSTANCE.get("textureDisplay");
	}
	
	protected void cleanup() {
		quad.onDestroy();
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
		
		//glBindFramebuffer(GL_FRAMEBUFFER, leftFrameBuffer);
		setViewport(0, 0, width, height);
		//value.render(leftEye, -1, -1);
		setShaderUniforms();
		quad.draw();
		//setViewport(width/2, 0, width/2, height);
		//value.render(rightEye, -1, -1);
		
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		setViewport(0, 0, width, height);
				
				//quad.draw();
	}

	
	private void setShaderUniforms() {
		quad.shader.ifPresent(s->{
			s.bind();
			leftEye.tellShader(s);
			s.trySetUniform("uptime", (System.currentTimeMillis()-startTime)/1000f); //casted to float
			s.trySetUniformTexture("renderedTexture", leftEyeTexture, 0);
			MatrixStack.projection.reset();
//			leftEye.loadProjectionMatrix();
			s.trySetMatrix("projectionMatrix", MatrixStack.projection.get());
			s.trySetUniform("sunColor", Color.WHITE.vec());
			s.trySetUniform("sunPos", rightEye.getLocation().pos);
			ShaderProgram.unbind();
		});
		quad.getLocation().setZ(0);
		quad.getLocation().setRotation(0, 0, 0);
	}
	
	@Override
	public void setScene(Scene s) {
		super.setScene(null);
		startTime = System.currentTimeMillis(); //refresh timer to help prevent strangeness at higher values
		super.setScene(s);
	}
	
	void fullscreenTexture(int textureID) {
		
		quad.draw();
	}
}
