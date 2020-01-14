package com.theincgi.lwjglApp.render;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDrawBuffer;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_MAX_LEVEL;
import static org.lwjgl.opengl.GL14.GL_GENERATE_MIPMAP;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_RENDERBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glBindRenderbuffer;
import static org.lwjgl.opengl.GL30.glFramebufferRenderbuffer;
import static org.lwjgl.opengl.GL30.glGenFramebuffers;
import static org.lwjgl.opengl.GL30.glGenRenderbuffers;
import static org.lwjgl.opengl.GL30.glRenderbufferStorage;
import static org.lwjgl.opengl.GL32.glFramebufferTexture;
import static org.lwjgl.opengl.GL45.*;

import java.util.ArrayList;
import java.util.Optional;

import org.lwjgl.openvr.Texture;
import org.lwjgl.openvr.VR;

import com.theincgi.lwjglApp.render.shaders.ShaderProgram;

public class RenderPipeline {
	private final ArrayList<ShaderProgram> steps = new ArrayList<ShaderProgram>();
	Model quadPostEffect;
	private static Location quadLocation = new Location(0, 0, 0, 0, 0, -90);
	private BufferSet a, b;
	private BufferSet dedicatedOut; //gets realllllly bad when textures alternate to the vr headset
	private int width;
	private int height;
	private long startTime;
	public final String[] channelNames;
	
	public RenderPipeline(int width, int height, String... channelNames) {
		quadPostEffect=ObjManager.INSTANCE.get("cmodels/plane/plane.obj", "post").get();
		this.width = width;
		this.height = height;
		a = new BufferSet(Math.min(32, channelNames.length));
		b = new BufferSet(Math.min(32, channelNames.length));
		dedicatedOut = new BufferSet(Math.min(32, channelNames.length));
		startTime = System.currentTimeMillis();
		this.channelNames = channelNames;
	}

	public synchronized void appendStep(ShaderProgram program) {
		steps.add(program);
	}
	public void bind() {
		glBindFramebuffer(GL_FRAMEBUFFER, b.frameBuffer);
	}
	public BufferSet process() {
		int stage = 0;
		for(int sh = 0; sh < steps.size(); sh++) {
			ShaderProgram s = steps.get(sh);
			if(sh < steps.size()-1) {
				swap();
				bind();
			}else
				glBindFramebuffer(GL_FRAMEBUFFER, dedicatedOut.frameBuffer);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); //Disabling this makes fun effects
			s.bind();
			s.trySetUniform("uptime", (System.currentTimeMillis()-startTime)/1000f);
			s.trySetUniform("stage", stage++);
			for(int i = 0; i<a.textureChannels.length; i++)
				if(!s.trySetUniformTexture(channelNames[i], a.textureChannels[i], i)) 
					s.disableTexture(channelNames[i], i);
				
			quadPostEffect.shader = Optional.of(s);
			quadPostEffect.drawAt(quadLocation);
		}
		return dedicatedOut;
	}

	private void swap() {
		BufferSet tmp = a;
		a = b;
		b = tmp;
	}

	
	
	public class BufferSet {
		public final int frameBuffer;
		public final int depthBuffer;
		public final int[] textureChannels;
		public final Texture output;
		public BufferSet(int nChannels) {
			textureChannels = new int[nChannels];
			
			
			int internalFormat = GL_RGBA8;
			int externalFormat = GL_RGBA;
			int textel         = GL_UNSIGNED_BYTE;

			frameBuffer = glGenFramebuffers();
			glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);

			for(int i = 0; i<Math.min(32, textureChannels.length); i++) {
				textureChannels[i] = glGenTextures();
				glBindTexture(GL_TEXTURE_2D, textureChannels[i]);
				glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, externalFormat, textel, 0);
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
				glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP, GL_FALSE);
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
			}


			depthBuffer = glGenRenderbuffers();
			glBindRenderbuffer(GL_RENDERBUFFER, depthBuffer);
			glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
			glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthBuffer);

			int[] drawBuffers = new int[Math.min(32, textureChannels.length)];
			
			for(int i = 0; i<Math.min(31, textureChannels.length); i++)
				glFramebufferTexture(GL_FRAMEBUFFER, drawBuffers[i] = GL_COLOR_ATTACHMENT0+i, textureChannels[i], 0);
			
			
			glDrawBuffers(drawBuffers);
			//unbinding
			glBindFramebuffer(GL_FRAMEBUFFER, 0);
			glBindTexture(GL_TEXTURE_2D, 0);
			glBindRenderbuffer(GL_RENDERBUFFER, 0);
			
			
			
			output = Texture.create();
			output.set(textureChannels[0], VR.ETextureType_TextureType_OpenGL, VR.EColorSpace_ColorSpace_Linear);
		}
	}
	
}
