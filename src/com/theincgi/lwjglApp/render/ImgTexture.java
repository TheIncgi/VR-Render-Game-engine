package com.theincgi.lwjglApp.render;

import static org.lwjgl.opengl.GL45.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.lwjgl.system.MemoryStack;

import com.theincgi.lwjglApp.misc.Logger;

public class ImgTexture implements AutoCloseable{
	private int HANDLE;
	
//	boolean blendHorizontal = true,
//			blendVertical   = true;
//	float   bumpMultiplier  = 1;
//	float   boost           = 0;
//	boolean colorCorrection = false; //only valid for Ka Kd and Ks maps
//	boolean clamp           = false;
	
	

	public ImgTexture(File imgfile) {
		int hndl = 0;
		try (MemoryStack mem = MemoryStack.stackPush()){
			BufferedImage img = ImageIO.read(imgfile);
			boolean isColor = img.getColorModel().getColorSpace().getNumComponents() > 1;
			ByteBuffer pixels = mem.malloc(img.getWidth() * img.getHeight() * (isColor?4:1));

			if(isColor)
				for (int y = 0; y < img.getHeight(); y++) {
					for (int x = 0; x < img.getWidth(); x++) {
						int p = img.getRGB(x, y); //argb
						pixels.put((byte) (p>>16 & 0xFF)); //r
						pixels.put((byte) (p>>8  & 0xFF)); //g
						pixels.put((byte) (p     & 0xFF)); //b
						pixels.put((byte) (p>>24 & 0xFF)); //a
					}
				}
			else
				for (int y = 0; y < img.getHeight(); y++) {
					for (int x = 0; x < img.getWidth(); x++) {
						int p = img.getRGB(x, y); //argb
						pixels.put((byte) (p     & 0xFF)); //b
					}
				}

			hndl = glGenTextures();
			glBindTexture(GL_TEXTURE_2D, hndl);

			int internal_format = isColor? GL_RGBA8 : GL_R8;
			int external_format = isColor? GL_RGBA  : GL_R;


			glTexImage2D(GL_TEXTURE_2D, 0, internal_format, img.getWidth(), img.getHeight(), 0, external_format, GL_UNSIGNED_BYTE, pixels);
			glGenerateMipmap(GL_TEXTURE_2D);

			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

			glBindTexture(GL_TEXTURE_2D, 0);
		} catch (IOException e) {
			Logger.preferedLogger.e("ImgTexture#new", "For file '"+imgfile.toString()+"'", e);
			if(hndl!=0) {
				glDeleteTextures(hndl);
				hndl = 0;
			}
		}finally {
			HANDLE = hndl;
		}

	}
	
	public int getHandle() {
		return HANDLE;
	}
	

	@Override
	public void close(){
		if(HANDLE != 0)
			glDeleteTextures(HANDLE);
		HANDLE = 0;
	}
}
