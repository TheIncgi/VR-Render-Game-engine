package com.theincgi.lwjglApp.render;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import com.theincgi.lwjglApp.misc.AbsManager;
import com.theincgi.lwjglApp.misc.Logger;
import com.theincgi.lwjglApp.misc.Pair;
import com.theincgi.lwjglApp.render.shaders.ShaderManager;
import com.theincgi.lwjglApp.render.shaders.ShaderProgram;

public class ObjManager extends AbsManager<Pair<File,Optional<ShaderProgram>>, Model>{
	public static final ObjManager INSTANCE = new ObjManager();
	private ObjManager() {}
	
	@Override
	protected Model load(Pair<File,Optional<ShaderProgram>> k) {
		try {
			Model m = new Model(k.x);
			m.shader = k.y;
			return m;
		} catch (IOException e) {
			Logger.preferedLogger.e("ObjManager", e);
			return null;
		}
	}
	@SuppressWarnings("deprecation")
	@Override
	protected void onUnload(Model t) {
		t.onDestroy();
	}
	
	public Optional<Model> get(String fileName, String sp){
		Optional<ShaderProgram> s = ShaderManager.INSTANCE.get(sp);
		return get(new Pair<>(new File(fileName) , s));
	}
	public Optional<Model> get(File file, String sp){
		Optional<ShaderProgram> s = ShaderManager.INSTANCE.get(sp);
		return get(new Pair<>(file , s));
	}
	
}
