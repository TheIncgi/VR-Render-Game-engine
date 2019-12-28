package com.theincgi.lwjglApp.misc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import javax.management.RuntimeErrorException;

//Plain text is cool and all, but it loads so much slower
public class ObjCompresser {
	
	
	private static class FaceGroup{
		String name;
		ArrayList<Integer> index = new ArrayList<>();
		boolean smooth = false;
		boolean useUV = false;
		boolean useNorm = false;
		FaceGroup(String matName){this.name = matName;}
	}
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
		
		int lineNumber = 0;
		ArrayList<Float> vertex = new ArrayList<>(),
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
					Collections.addAll(vertex, splitFloats(line)); break;
				case "vt":
					Collections.addAll(uv, splitFloats(line)); break;
				case "vn":
					Collections.addAll(normal, splitFloats(line)); break;
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
			
			//abc hash equals
			class Triplet{int a,b,c;Triplet(int a,int b, int c){this.a=a;this.b=b;this.c=c;} public boolean equals(Triplet obj) {
					return this==obj || (a==obj.a && b==obj.b && c==obj.c);}
			@Override public int hashCode() {	final int prime = 31;int result = 1;result = prime * result + a;result = prime * result + b;result = prime * result + c;
				return result;	}
			@Override public boolean equals(Object obj) {
				if (this == obj)return true;if (obj == null)return false;if (getClass() != obj.getClass())return false;
				Triplet other = (Triplet) obj;	return equals(other);}}
			
			HashMap<Triplet, Integer> recorded = new HashMap<>();
			ArrayList<Triplet> allTripplets = new ArrayList<>();
			ArrayList<Float> finalVertex = new ArrayList<>(), finalUV = new ArrayList<>(), finalNormal = new ArrayList<>();
			ArrayList<FaceGroup> finalFaceGroups = new ArrayList<>();
			int pos = 0;
			for (FaceGroup faceGroup : faceGroups) {
				FaceGroup ffg = new FaceGroup(faceGroup.name);
				ffg.smooth = faceGroup.smooth;
				ffg.useNorm = faceGroup.useNorm;
				ffg.useUV = faceGroup.useUV;
				finalFaceGroups.add(ffg);
				for (int i = 0; i < faceGroup.index.size(); i+=3) {
					int a = faceGroup.index.get(i);
					int b = faceGroup.index.get(i+1);
					int c = faceGroup.index.get(i+2);
					Triplet x = new Triplet(a, b, c);
					if(!recorded.containsKey(x)) {
						allTripplets.add(x);
						recorded.put(x, pos++);
					}
					ffg.index.add(recorded.get(x));
				}
			}
			
			for (int i = 0; i < allTripplets.size(); i++) {
				Triplet t = allTripplets.get(i);
				finalVertex.add( vertex.get(t.a*3) );
				finalVertex.add( vertex.get(t.a*3+1) );
				finalVertex.add( vertex.get(t.a*3+2) );
				finalUV.add(         t.b==-1?-1:    uv.get(t.b*3) );
				finalUV.add(         t.b==-1?-1:    uv.get(t.b*3+1) );
				finalUV.add(         t.b==-1?-1:    uv.get(t.b*3+2) );
				finalNormal.add(     t.c==-1?-1:normal.get(t.c*3) );
				finalNormal.add(     t.c==-1?-1:normal.get(t.c*3+1) );
				finalNormal.add(     t.c==-1?-1:normal.get(t.c*3+2) );
			}
			
			
			
			writeFile(finalVertex, finalUV, finalNormal, objName, finalFaceGroups, out);
			
			
			
			
		} catch (IOException e) {
			Logger.preferedLogger.e("ObjCompressor#compress", e);
		}catch (Exception e) {
			throw new RuntimeException("On line "+lineNumber+ ": "+op+" "+line, e);
		}
	}





	private static void writeFile(ArrayList<Float> vertex, ArrayList<Float> uv, ArrayList<Float> normal, String objName,
			ArrayList<FaceGroup> faceGroups, RandomAccessFile out) throws IOException {
		//Output file:
		out.writeUTF(objName);
		
		out.writeInt(vertex.size());
		for(float d : vertex)
			out.writeFloat(d);
		
		out.writeInt(uv.size());
		for(float u : uv)
			out.writeFloat(u);
		
		out.writeInt(normal.size());
		for(float d : normal)
			out.writeFloat(d);
		
		int pos = 0;
		int totalIndex = 0;
		for (FaceGroup f : faceGroups) 
			totalIndex+=f.index.size();
		
		out.writeInt(totalIndex);
		out.writeInt(faceGroups.size());
		for (FaceGroup fg : faceGroups) {
			out.writeUTF(fg.name);
			out.writeInt(pos);
			out.writeInt(pos+fg.index.size()-1); //inclusive according to glDrawRanged javadoc
			out.writeBoolean(fg.smooth);
			out.writeBoolean(fg.useUV);
			out.writeBoolean(fg.useNorm);
//				out.writeInt(fg.index.size());
			for(int i : fg.index)
				out.writeInt(i);
			pos+=fg.index.size();
		}
	}
	
	
	


	private static Float[] splitFloats(String line) {
		line = line.trim();
		String[] x = line.split(" ");
		Float[] out = new Float[x.length];
		for (int i = 0; i < out.length; i++) {
			out[i] = Float.parseFloat(x[i]);
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

	
	public static void compressAll(boolean overwrite) throws IOException {
		File folder = new File("models");
		File out = new File("cmodels");
		out.mkdir();
		for (File f : folder.listFiles()) {
			if(f.isDirectory()) {
				for(File m : f.listFiles()) {
					File target = new File(out, f.getName()+File.separatorChar+m.getName());
					if(target.exists() && !overwrite) continue;
					target.getParentFile().mkdirs();
					if(m.getName().endsWith(".obj")) {
						compress(m, target);
					}else {
						if(target.exists()) target.delete();
						Files.copy(m.toPath(), target.toPath());
					}
					System.out.println("Updated "+target.getParentFile().getName() + "/"+target.getName());
				}
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		compressAll(false);//(new File("models/walkyCube/walkyCube_000001.obj"), new File("Compress.test"));
	}
}
