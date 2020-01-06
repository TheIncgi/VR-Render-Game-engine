package com.theincgi.lwjglApp.render;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import com.theincgi.lwjglApp.misc.AbsManager;
import com.theincgi.lwjglApp.misc.Logger;

public class ObjManager extends AbsManager<File, Model>{
	public static final ObjManager INSTANCE = new ObjManager();
	private ObjManager() {}
	
	@Override
	protected Model load(File k) {
		try {
			return new Model(k);
		} catch (IOException e) {
			Logger.preferedLogger.e("ObjManager", e);
			return null;
		}
	}
	@Override
	protected void onUnload(Model t) {
		t.onDestroy();
	}
	
	public Optional<Model> get(String fileName){
		return get(new File(fileName));
	}
	
	
}
