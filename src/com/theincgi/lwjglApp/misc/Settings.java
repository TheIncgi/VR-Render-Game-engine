package com.theincgi.lwjglApp.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import com.theincgi.lwjglApp.render.vr.VRController;

public class Settings {
	private static File runFolder;
	private static File dataFile;
	private static boolean dirty = false;
	private static HashMap<String, Object> properties;

	/**Controls prefix<br>
	 * Value: <code>controls.</code>
	 * */
	public static final String CONTROLS = "button_controls.";
	
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
	

	public static Object computeIfAbsent(String key, Function<String, Object> ifAbs) {
		return properties.computeIfAbsent(key, ifAbs);
	}
	public static int computeIntIfAbsent(String key, Function<String, Integer> ifAbs) {
		Object o = properties.get(key);
		int x = 0;
		if(o==null || !(o instanceof Integer)) 
			properties.put(key, x = ifAbs.apply(key));
		return x;
	}
	public static float computeFloatIfAbsent(String key, Function<String, Float> ifAbs) {
		Object o = properties.get(key);
		float x = 0;
		if(o==null || !(o instanceof Float)) 
			properties.put(key, x = ifAbs.apply(key));
		return x;
	}
	public static long computeLongIfAbsent(String key, Function<String, Long> ifAbs) {
		Object o = properties.get(key);
		long x = 0;
		if(o==null || !(o instanceof Long)) 
			properties.put(key, x = ifAbs.apply(key));
		return x;
	}
	public static String computeStringIfAbsent(String key, Function<String, String> ifAbs) {
		Object o = properties.get(key);
		String x = null;
		if(o==null || !(o instanceof String)) 
			properties.put(key, x = ifAbs.apply(key));
		return x;
	}
	
	/**dont forget to prefix controls constant!<br>
	 * Creates a blank hashmap if missing*/
	public static HashMap<VRController.Input, String> computeControllMappingIfAbsent(String key){
		return computeControllMappingIfAbsent(key, k->new HashMap<>());
	}
	/**dont forget to prefix controls constant!*/
	public static HashMap<VRController.Input, String> computeControllMappingIfAbsent(String key, Function<String, HashMap<VRController.Input, String>> ifAbs) {
		Object o = properties.get(key);
		HashMap<VRController.Input, String> x = null;
		if(o==null || !(o instanceof HashMap)) 
			properties.put(key, x = ifAbs.apply(key));
		return x;
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
	
	public static File getRunFolder() {
		return runFolder;
	}
}
