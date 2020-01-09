package com.theincgi.lwjglApp.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.util.HashMap;

public class Settings {
	private static File runFolder;
	private static File dataFile;
	private static boolean dirty = false;
	private static HashMap<String, Object> properties;

	static {
		runFolder = Paths.get("").toAbsolutePath().normalize().toFile();
		dataFile = new File(runFolder, "gameData.dat");
		Logger.preferedLogger.i("Settings#static", "Data file is: "+dataFile.toString());
		dirty = !dataFile.exists();
	}
	private Settings() {}

	public static void save() {
		if(!dirty) return;
		try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataFile))){
			oos.writeObject(properties);
			Logger.preferedLogger.i("Settings#save", "Settings have been saved");
		} catch (Exception e) {
			Logger.preferedLogger.e("Settings#save", e);
		}
	}
	@SuppressWarnings("unchecked")
	public static void load() {
		if(dataFile.exists())
			try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFile))){
				Object i = ois.readObject();
				if (i instanceof HashMap<?,?>)
					properties = (HashMap<String, Object>) i;
			} catch (ClassNotFoundException | IOException e) {
				Logger.preferedLogger.e("Settings#load", e);
			}
		if(properties==null) {
			properties = new HashMap<>();
			Logger.preferedLogger.i("Settings#load", "A new settings table has been created");
		}
	}

	public static Object get(String key) {
		return properties.get(key);
	}
	public static Object get(String key, Object def) {
		return properties.getOrDefault(key, def);
	}
	public static int getInt(String key, int def) {
		Object o = properties.getOrDefault(key, def);
		if (o instanceof Integer)
			return (Integer) o;
		return def;
	}
	public static float getFloat(String key, float def) {
		Object o = properties.getOrDefault(key, def);
		if (o instanceof Float)
			return (Float) o;
		return def;
	}
	public static long getLong(String key, long def) {
		Object o = properties.getOrDefault(key, def);
		if (o instanceof Long)
			return (Long) o;
		return def;
	}
	public static String getString(String key, String def) {
		Object o = properties.getOrDefault(key, def);
		if (o instanceof Integer)
			return (String) o;
		return def;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getType(String key, T def) {
		Object o = properties.getOrDefault(key, def);
		try {
			return (T) o;
		}catch (ClassCastException e) {
			return def;
		}
	}
	
	public static void put(String key, Object value) {
		properties.put(key, value);
		dirty = true;
	}
}
