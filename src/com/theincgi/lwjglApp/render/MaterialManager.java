package com.theincgi.lwjglApp.render;

import java.io.File;
import java.io.IOException;

import com.theincgi.lwjglApp.misc.AbsManager;
import com.theincgi.lwjglApp.misc.Logger;

public class MaterialManager extends AbsManager<File, MaterialGroup>{
	
	public static final MaterialManager INSTANCE = new MaterialManager(); 
	
	@Override
	protected MaterialGroup load(File key) {
		try{
			return new MaterialGroup(key);
		}catch (Exception e) {
			Logger.preferedLogger.e("MaterialManager", e);
			return null;
		}
	}

	@Override
	protected void onUnload(MaterialGroup t) {
	}
	
}
