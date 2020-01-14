package com.theincgi.lwjglApp.render;

import java.io.File;
import java.util.Optional;

import com.theincgi.lwjglApp.misc.AbsManager;
import com.theincgi.lwjglApp.misc.Logger;

public class TextureManager extends AbsManager<File, ImgTexture>{
	
	public static final TextureManager INSTANCE = new TextureManager();
	
	
	
	public Optional<ImgTexture> get(String path) {
		File f = new File(path);
		return get(f);
	}
	
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
