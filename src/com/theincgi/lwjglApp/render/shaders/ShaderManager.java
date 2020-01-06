package com.theincgi.lwjglApp.render.shaders;

import java.io.File;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import com.theincgi.lwjglApp.misc.AbsManager;

public class ShaderManager extends AbsManager<String, ShaderProgram>{
	private static File shadersFolder = new File("shaders");
	public static boolean autoRefreshShaders = false;
	
	public static final ShaderManager INSTANCE = new ShaderManager();
	
	static{
		shadersFolder.mkdir();
	}
	
	/**
	 * Gets or loads a shader where files are &lt;name&gt;.vs and &lt;name&gt;.fs
	 * */
	public Optional<ShaderProgram> get(String name) {
		Optional<ShaderProgram> x = super.get(name);
		x.ifPresent(y->y.autoRefresh(autoRefreshShaders));
		return x;
	}

	@Override
	protected ShaderProgram load(String k) {
		File vs = new File(shadersFolder, k+".vert");
		File fs = new File(shadersFolder, k+".frag");
		return new ShaderProgram(vs, fs);
	}

	@Override
	protected void onUnload(ShaderProgram sp) {
		sp.delete();
	}
	
}
