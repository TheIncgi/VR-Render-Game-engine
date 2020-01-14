package com.theincgi.lwjglApp;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.openvr.HmdMatrix34;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import com.theincgi.lwjglApp.render.vr.VRUtil;

public class Utils {
	public static final Vector3f 
		AXIS_UP = new Vector3f(0, 1, 0),
		AXIS_RIGHT = new Vector3f(1, 0, 0),
		AXIS_OUT = new Vector3f(0, 0, 1);
	
	
	/**Exclusive In range test*/
	public static boolean inRangeE(float x, float min, float max) {
		return min<=x && x < max;
	}
	/**Exclusive In range test*/
	public static boolean inRangeE(int x, int min, int max) {
		return min<=x && x < max;
	}
	/**Exclusive In range test*/
	public static boolean inRangeE(long x, long min, long max) {
		return min<=x && x < max;
	}
	
	/**Inclusive In range test*/
	public static boolean inRangeI(float x, float min, float max) {
		return min<=x && x <= max;
	}
	/**Inclusive In range test*/
	public static boolean inRangeI(int x, int min, int max) {
		return min<=x && x <= max;
	}
	/**Inclusive In range test*/
	public static boolean inRangeI(long x, long min, long max) {
		return min<=x && x <= max;
	}
	
	public static double max(double a, double... args) {
		double m = a;
		for (int i = 0; i < args.length; i++) {
			m = Math.max(m, args[i]);
		}
		return m;
	}
	public static double min(double a, double... args) {
		double m = a;
		for (int i = 0; i < args.length; i++) {
			m = Math.max(m, args[i]);
		}
		return m;
	}
	public static float max(float a, float... args) {
		float m = a;
		for (int i = 0; i < args.length; i++) {
			m = Math.max(m, args[i]);
		}
		return m;
	}
	public static float min(float a, float... args) {
		float m = a;
		for (int i = 0; i < args.length; i++) {
			m = Math.max(m, args[i]);
		}
		return m;
	}
	
	public static FloatBuffer toBuffer(float... floats) {
		FloatBuffer buf = MemoryUtil.memAllocFloat(floats.length);
		buf.put(floats).flip();
		return buf;
	}
	public static IntBuffer toBuffer(int... ints) {
		IntBuffer buf = MemoryUtil.memAllocInt(ints.length);
		buf.put(ints).flip();
		return buf;
	}
	public static void freeBuffer(Buffer buf) {
		if(buf!=null)
			MemoryUtil.memFree(buf);
	}
	
	public static Matrix4f fromT(HmdMatrix34 h) {
		return fromT(h, new Matrix4f());
	}
	/**adds an extra row with identity and transposes*/
	public static Matrix4f fromT(HmdMatrix34 h, Matrix4f dest) {
		Matrix4f out = dest ==null? new Matrix4f() : dest;
		out.m00 = h.m(0);  out.m10 = h.m(1);   out.m20 = h.m(2); out.m30 = h.m(3);
		out.m01 = h.m(4);  out.m11 = h.m(5);   out.m21 = h.m(6); out.m31 = h.m(7);
		out.m02 = h.m(8);  out.m12 = h.m(9);   out.m22 = h.m(10);out.m32 = h.m(11);
		/*   03 = 0             13 =0               23 = 0    */ out.m33 = 1;                                     
		return out;
	}
	
	
	/**adds an extra row with identity and transposes*/
	public static Matrix4f from(HmdMatrix34 h) { 
		Matrix4f out = new Matrix4f();
		out.m00 = h.m(0);  out.m10 = h.m(4);  out.m20 = h.m(8);  //  m30 = 0
		out.m01 = h.m(1);  out.m11 = h.m(5);  out.m21 = h.m(9);  //  m31 = 0 
		out.m02 = h.m(2);  out.m12 = h.m(6);  out.m22 = h.m(10); //  m32 = 0
		out.m03 = h.m(3);  out.m13 = h.m(7);  out.m23 = h.m(11); out.m33 = 1;
		return out;
	}
	/**
	 * Takes 2 length 3 float arrays and provides the distance<br>
	 * longer arrays will only use the first 3 elements
	 * */
	public static float distVec3(float[] a, float[] b) {
		float dx = b[0] - a[0],
			  dy = b[1] - a[1],
			  dz = b[2] - a[2];
		return (float) Math.sqrt( dx*dx + dy*dy + dz*dz );
	}
	
	/**Returns a normaly distributed float between -range and range, non inclusive*/
	public static float ndRandom(float range) {
		return (float)( Math.random()+Math.random()+Math.random() - Math.random() - Math.random() - Math.random() )/6*range;
	}
}
