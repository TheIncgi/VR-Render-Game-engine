package com.theincgi.lwjglApp.render;

import java.io.File;

import com.theincgi.lwjglApp.misc.AbsManager;
import com.theincgi.lwjglApp.misc.Logger;

public class TextureManager extends AbsManager<File, ImgTexture>{
	
	public static final TextureManager INSTANCE = new TextureManager();
	
	
	protected ImgTexture load(File key) {
		try {
			return new ImgTexture(key);
		}catch (Exception e) {
			Logger.preferedLogger.e("TextureManager", e);
			return null;
		}
	};
	@Override
	protected void onUnload(ImgTexture t) {
		t.close();
	}
}
