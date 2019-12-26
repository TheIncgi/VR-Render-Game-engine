package com.theincgi.lwjglApp.misc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

import javax.management.RuntimeErrorException;

//Plain text is cool and all, but it loads so much slower
public class ObjCompresser {
	
	/**
	 * Header:
	 * UTF-String name
	 * 
	 * int vertex count
	 * ...float float float x count
	 * 
	 * int uv count
	 * ...
	 * 
	 * int normal count
	 * ...
	 *
	 * {*for materials*
	 * 		UTF-String materialName
	 *		smooth
	 *
	 *		int face count
	 * 		...
	 * }
	 * */
	
	public static void compress(File objFile, File outFile) {
		class FaceGroup{
			String name;
			ArrayList<Integer> index = new ArrayList<>();
			boolean smooth = false;
			boolean useUV = false;
			boolean useNorm = false;
			FaceGroup(String matName){this.name = matName;}
		}
		int lineNumber = 0;
		ArrayList<Double> vertex = new ArrayList<>(),
		                  uv = new ArrayList<>(),
		                  normal = new ArrayList<>();
		String objName = "UNNAMED";	
		ArrayList<FaceGroup> faceGroups = new ArrayList<>();
		FaceGroup activeFaceGroup = null;
		                  
		String op = null, line = null;
		try(RandomAccessFile out = new RandomAccessFile(outFile, "rw"); Scanner in = new Scanner(objFile);){
			
			while(in.hasNextLine()) {
				op = in.next();
				line = in.nextLine();
				lineNumber++;
				if(op.startsWith("#") || op.isBlank()) continue;
				switch (op) {
				case "mtllib":
					break;
				case "o":
					objName = line; 
					break;
				case "v":
					Collections.addAll(vertex, splitDoubles(line)); break;
				case "vt":
					Collections.addAll(uv, splitDoubles(line)); break;
				case "vn":
					Collections.addAll(normal, splitDoubles(line)); break;
				case "g": //group
					break;
				case "usemtl":
					faceGroups.add(activeFaceGroup = new FaceGroup(line));
					break;
				case "s":
					activeFaceGroup.smooth = line.equals("1");
					break;
				case "f":
					if(activeFaceGroup.index.size()==0) {
						switch(count(line, "/")) {
						case 0: //face only
							break; //already false
						case 6: //all or we are defaulting the missing to -1 for x//z where y is /y/ but it isn't
							activeFaceGroup.useNorm = true; //fall thru to enable uv
						case 3: //face + texture
							activeFaceGroup.useUV = true;
							break;
						default:
							Logger.preferedLogger.e("ObjCompresser", new RuntimeException("Unexpected number of / ("+count(line,"/")+")"));							
						}
					}
					Collections.addAll(activeFaceGroup.index, splitInt(line));
					break;
				default:
					Logger.preferedLogger.e("ObjCompresser", new RuntimeException("Unexpected line operation: "+op));
				}
				
			}
			
			StringBuilder sizes = new StringBuilder();
			for (FaceGroup faceGroup : faceGroups) {
				sizes.append(faceGroup.name)
				.append(": ")
				.append(faceGroup.index.size())
				.append("\n");
			}
			System.out.printf("Finished parsing %s\n"
					+ "\tVerts: %d\n"
					+ "\tUV: %d\n"
					+ "\tNorm: %d\n"
					+ "\tMaterialGroups: %d\n"
					+ "\tFaces: %s",
					objFile.getName(),
					vertex.size(),
					uv.size(),
					normal.size(),
					faceGroups.size(),
					sizes
				);
			
			//Output file:
			out.writeUTF(objName);
			
			out.writeInt(vertex.size());
			for(Double d : vertex)
				out.writeDouble(d);
			
			out.writeInt(uv.size());
			for(Double u : uv)
				out.writeDouble(u);
			
			out.writeInt(normal.size());
			for(Double d : normal)
				out.writeDouble(d);
			
			for (FaceGroup fg : faceGroups) {
				out.writeUTF(fg.name);
				out.writeBoolean(fg.smooth);
				out.writeBoolean(fg.useUV);
				out.writeBoolean(fg.useNorm);
				out.writeInt(fg.index.size());
				for(int i : fg.index)
					out.writeInt(i);
			}
			
			
			
			
		} catch (IOException e) {
			Logger.preferedLogger.e("ObjCompressor#compress", e);
		}catch (Exception e) {
			throw new RuntimeException("On line "+lineNumber+ ": "+op+" "+line, e);
		}
	}
	
	
	


	private static Double[] splitDoubles(String line) {
		line = line.trim();
		String[] x = line.split(" ");
		Double[] out = new Double[x.length];
		for (int i = 0; i < out.length; i++) {
			out[i] = Double.parseDouble(x[i]);
		}
		return out;
	}
	private static Integer[] splitInt(String line) {
		line = line.trim();
		String[] x = line.split("[ /]");
		Integer[] out = new Integer[x.length];
		for (int i = 0; i < out.length; i++) {
			out[i] = Integer.parseInt("0"+x[i])-1;
		}
		return out;
	}
	private static int count(String x, String y) {
		int z = -1, m = 0;
		while((z = x.indexOf(y, z+1)) > 0) m++;
		return m;
	}

	
	public static void main(String[] args) {
		compress(new File("models/walkyCube/walkyCube_000001.obj"), new File("Compress.test"));
	}
}
