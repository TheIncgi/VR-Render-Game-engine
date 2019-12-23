package com.theincgi.lwjglApp.render;

import java.io.File;

import com.theincgi.lwjglApp.misc.AbsManager;

public class ObjManager extends AbsManager<File, ObjModel>{
	public static final ObjManager INSTANCE = new ObjManager();
	private ObjManager() {}
	
	@Override
	protected ObjModel load(File k) {
		return null;
	}

	@Override
	protected void onUnload(ObjModel t) {
		
	}

	
	
}
