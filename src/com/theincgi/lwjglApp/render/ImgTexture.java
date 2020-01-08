package com.theincgi.lwjglApp.render;

import static org.lwjgl.opengl.GL45.*;

import java.awt.Graphics;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Hashtable;
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
		ByteBuffer pixels;
		try {
			BufferedImage img = ImageIO.read(imgfile);
			int components = img.getColorModel().getColorSpace().getNumComponents();
			boolean isColor = components > 1;
			pixels = ByteBuffer.allocate(img.getWidth() * img.getHeight() * (isColor?4:1));
			pixels.order(ByteOrder.nativeOrder());

			if(isColor) {
				int[] data = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
			    pixels = ByteBuffer.allocateDirect(data.length * 4);
			    for (int pixel : data) {
			    	pixels.put((byte) ((pixel >> 16) & 0xFF));
			    	pixels.put((byte) ((pixel >> 8)  & 0xFF));
			    	pixels.put((byte) ( pixel        & 0xFF));
			    	pixels.put((byte) ( pixel >> 24        ));
			    }
			}else {
				int[] data = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
			    pixels = ByteBuffer.allocateDirect(data.length);
			    for (int pixel : data) {
			    	pixels.put((byte) ( pixel        & 0xFF));
			    }
			}
				
//			pixels.position();
//			pixels.limit();
			pixels.flip();
			hndl = glGenTextures();
			glBindTexture(GL_TEXTURE_2D, hndl);

			int internal_format = isColor? GL_RGBA8 : GL_R8;
			int external_format = isColor? GL_RGBA  : GL_R;

			glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			
			glTexImage2D(GL_TEXTURE_2D, 0, internal_format, img.getWidth(), img.getHeight(), 0, external_format, GL_UNSIGNED_BYTE, pixels);
			glGenerateMipmap(GL_TEXTURE_2D);
			
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
	

	//https://stackoverflow.com/questions/5194325/how-do-i-load-an-image-for-use-as-an-opengl-texture-with-lwjgl
	/**
	 * Convert the buffered image to a texture
	 */
	private ByteBuffer convertImageData(BufferedImage bufferedImage) {
	    ByteBuffer imageBuffer;
	    WritableRaster raster;
	    BufferedImage texImage;

	    ColorModel glAlphaColorModel = new ComponentColorModel(ColorSpace
	            .getInstance(ColorSpace.CS_sRGB), new int[] { 8, 8, 8, 8 },
	            true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);

	    raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
	            bufferedImage.getWidth(), bufferedImage.getHeight(), 4, null);
	    texImage = new BufferedImage(glAlphaColorModel, raster, true,
	            new Hashtable<>());

	    // copy the source image into the produced image
	    Graphics g = texImage.getGraphics();
	    g.setColor(new java.awt.Color(0f, 0f, 0f, 0f));
	    g.fillRect(0, 0, 256, 256);
	    g.drawImage(bufferedImage, 0, 0, null);

	    // build a byte buffer from the temporary image
	    // that be used by OpenGL to produce a texture.
	    byte[] data = ((DataBufferByte) texImage.getRaster().getDataBuffer())
	            .getData();

	    imageBuffer = ByteBuffer.allocateDirect(data.length);
	    imageBuffer.order(ByteOrder.nativeOrder());
	    imageBuffer.put(data, 0, data.length);
	    imageBuffer.flip();

	    return imageBuffer;
	}
	
	@Override
	public void close(){
		if(HANDLE != 0)
			glDeleteTextures(HANDLE);
		HANDLE = 0;
	}
}
