package com.theincgi.lwjglApp.render.text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.lwjgl.util.vector.Vector4f;

import com.theincgi.lwjglApp.misc.Logger;
import com.theincgi.lwjglApp.render.ImgTexture;
import com.theincgi.lwjglApp.render.TextureManager;

public class FontTexture {
	private ImgTexture texPlain, texBold;
	private int tileWidth, tileHeight, perRow, ascent, descent, leading, utfStart, utfEnd;
	private int[] widths;
	/**For this to load the matching font resoureces must exist, FontTextures.get will return the closest match
	 * @throws IOException 
	 * @throws FileNotFoundException */
	public FontTexture(String name, int size) throws FileNotFoundException, IOException {
		File img = new File(FontTextures.fontsFolder, name+"_"+size+".png");
		File imgBld = new File(FontTextures.fontsFolder, name+"_Bold_"+size+".png");
		File dat = new File(FontTextures.fontsFolder, name+"_"+size+".chw");
		if(!img.exists() && !dat.exists())
			throw new FileNotFoundException("Missing files: ["+img+","+dat+"]");
		if(!img.exists())
			throw new FileNotFoundException("Missing file: ["+img+"]");
		if(!dat.exists())
			throw new FileNotFoundException("Missing file: ["+dat+"]");
		
		try(RandomAccessFile raf = new RandomAccessFile(dat, "rw");){
			tileWidth  = raf.readInt();
			tileHeight = raf.readInt();
			perRow     = raf.readInt();
			ascent     = raf.readInt();
			descent    = raf.readInt();
			leading    = raf.readInt();
			utfStart   = raf.readInt();
			utfEnd     = raf.readInt();
			widths = new int[utfEnd-utfStart+1];
			for(int i = 0; i<widths.length; i++)
				widths[i] = raf.readInt();
		}
		
		texPlain = TextureManager.INSTANCE.get(img).orElseThrow(()->new IOException("Unable to load texture"));
		texBold = TextureManager.INSTANCE.get(imgBld).orElseThrow(()->new IOException("Unable to load bold texture"));
		if(texPlain.getSrcWidth()!=texBold.getSrcWidth() || texPlain.getSrcHeight()!=texBold.getSrcHeight()) throw new IOException("Plane and bold image resources contain different dimensions");
		Logger.preferedLogger.i("FontTexture#new", "Font '"+name+" "+size+"' has been loaded");
	}
	
	public Vector4f getUV(int codepoint) {
		codepoint-=utfStart;
		int x = codepoint % perRow;
		int y = codepoint / perRow;
		int px1 = x*tileWidth;
		int py1 = y*tileHeight;
		int px2 = px1+tileWidth;
		int py2 = py1+tileHeight;
		return new Vector4f(px1/(float)texPlain.getSrcWidth(), py1/(float)texPlain.getSrcHeight(), px2/(float)texPlain.getSrcWidth(), py2/(float)texPlain.getSrcHeight());		
	}
	public int getPerRow() {
		return perRow;
	}
	public int getAscent() {
		return ascent;
	}
	public int getDescent() {
		return descent;
	}
	public int getLeading() {
		return leading;
	}
	public int getUtfStart() {
		return utfStart;
	}
	public int getUtfEnd() {
		return utfEnd;
	}
	public boolean has(int codepoint) {
		return utfStart <= codepoint && codepoint <= utfEnd;
	}
	public int getCharWidth(int codepoint) {
		codepoint-=utfStart;
		if(codepoint > utfEnd) return 0;
		return widths[codepoint];
	}
	
	public int getTileWidth() {
		return tileWidth;
	}
	public int getTileHeight() {
		return tileHeight;
	}
	
	public ImgTexture getTexPlain() {
		return texPlain;
	}
	public ImgTexture getTexBold() {
		return texBold;
	}
	
}
