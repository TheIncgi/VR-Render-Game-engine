package com.theincgi.lwjglApp.render.shaders;

import java.io.File;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Consumer;

public class ShaderManager {
	private static WeakHashMap<String, ShaderProgram> cache = new WeakHashMap<>();
	private static File shadersFolder = new File("shaders");
	
	public static boolean autoRefreshShaders = true;
	static{
		shadersFolder.mkdir();
	}
	
	private ShaderManager() {}
	
	
	/**
	 * Gets or loads a shader where files are &lt;name&gt;.vs and &lt;name&gt;.fs
	 * */
	public static Optional<ShaderProgram> get(String name) {
		return Optional.ofNullable( cache.computeIfAbsent(name, k->{
			return loadShader(k).autoRefresh(autoRefreshShaders);
		}));
	}

	public static void forLoaded(Consumer<ShaderProgram> each) {
		for (ShaderProgram shader : cache.values()) {
			each.accept(shader);
		}
	}
	
	private static ShaderProgram loadShader(String k) {
		File vs = new File(shadersFolder, k+".vs");
		File fs = new File(shadersFolder, k+".fs");
		return new ShaderProgram(vs, fs);
	}
	
}
