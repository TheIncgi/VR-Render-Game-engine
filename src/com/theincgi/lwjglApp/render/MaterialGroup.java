package com.theincgi.lwjglApp.render;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Scanner;

import org.lwjgl.openvr.Texture;

import com.theincgi.lwjglApp.misc.Logger;
import com.theincgi.lwjglApp.ui.Color;

public class MaterialGroup implements Cloneable {
	public final HashMap<String, Material> materials = new HashMap<>();

	private MaterialGroup() {}
	
	public MaterialGroup(File src) {
		try(Scanner scanner = new Scanner(src)){
			Material current = null;
			while(scanner.hasNext()) {
				String op = scanner.next();
				if(op.startsWith("#")) {scanner.nextLine(); continue;}
				if(current==null && op.equals("newmtl")) {
					current = new Material(scanner.next());
					materials.put(current.materialName, current);
				}else if(current!=null)
					switch (op) {
					case "newmtl":
						current = new Material(scanner.next());
						materials.put(current.materialName, current);
						break;
					case "Kd":
						current.kd = new Color(scanner.nextFloat(), scanner.nextFloat(), scanner.nextFloat());
						break;
					case "Ka":
						current.ka = nextColor(scanner);
						break;
					case "Ks":
						current.ks = nextColor(scanner);
						break;
					case "Ke":
						current.ke = nextColor(scanner);
						break;
					case "Tf":
						current.tf = nextColor(scanner);
						break;
					case "illum":
						current.illum = scanner.nextInt();
						break;
					case "d":{String x = scanner.next(); //halo
					if(x.equals("-halo")) {
						current.halo = Optional.of(true);
						x = scanner.next();
					}
					current.d = Optional.of(Float.parseFloat(x));
					break;
					}
					case "Ns":
						current.ns = Optional.of(scanner.nextFloat());
						break;
					case "sharpness":
						current.sharpness = Optional.of(scanner.nextFloat());
						break;
					case "Ni":
						current.ni = Optional.of(scanner.nextFloat());
						break;
					case "map_Ka":
						current.map_ka = nextImage(src, scanner); break;
					case "map_Kd":
						current.map_kd = nextImage(src, scanner); break;
					case "map_Ks":
						current.map_ks = nextImage(src, scanner); break;
					case "map_Ns":
						current.map_ns = nextImage(src, scanner); break;
					case "map_d":
						current.map_d = nextImage(src, scanner); break;
					case "map_disp":
						current.map_disp = nextImage(src, scanner); break;
					case "map_bump":
						current.map_bump = nextImage(src, scanner); break;
					default:
						throw new RuntimeException("Unexpected property type '"+op+"' in file "+src.toString());
					}
			}
		}catch (Exception e) {
			Logger.preferedLogger.e("MaterialGroup#new", e);
		}
	}
	
	/**Creates a 'deep' copy for modification<br>
	 * any optional type values may not have cloned their held values*/
	@Override
	public MaterialGroup clone() {
		MaterialGroup x = new MaterialGroup();
		for(Entry<String, Material> a : materials.entrySet())
			x.materials.put(a.getKey(), a.getValue().clone());
		return x;
	}

	private static Optional<Color> nextColor(Scanner s){
		return Optional.of(new Color(s.nextFloat(), s.nextFloat(), s.nextFloat()));
	}
	private static Optional<ImgTexture> nextImage(File file, Scanner s){
		String line = s.nextLine();
		int x = line.lastIndexOf(" ");
		return TextureManager.INSTANCE.get(new File(file.getParentFile(), line.substring(x+1)));
	}
}
