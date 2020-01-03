package com.theincgi.lwjglApp;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.openvr.HmdMatrix34;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

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
		MemoryUtil.memFree(buf);
	}
	
	/**adds an extra row with identity and transposes*/
	public static Matrix4f fromT(HmdMatrix34 h) {
		Matrix4f out = new Matrix4f();
		out.m00 = h.m(0);  out.m01 = h.m(4);  out.m02 = h.m(8);  //0
		out.m10 = h.m(1);  out.m11 = h.m(5);  out.m12 = h.m(9);  //0
		out.m20 = h.m(2);  out.m21 = h.m(6);  out.m22 = h.m(10);  //0
		out.m30 = h.m(3);  out.m31 = h.m(7); out.m32 = h.m(11); out.m33 = 1;
		return out;
	}
	
	/**adds an extra row with identity and transposes*/
	public static Matrix4f from(HmdMatrix34 h) {
		Matrix4f out = new Matrix4f();
		out.m00 = h.m(0);  out.m01 = h.m(1);  out.m02 = h.m(2);  out.m03 = h.m(3);
		out.m10 = h.m(4);  out.m11 = h.m(5);  out.m12 = h.m(6);  out.m13 = h.m(7);
		out.m20 = h.m(8);  out.m21 = h.m(9);  out.m22 = h.m(10); out.m23 = h.m(11);
		/*0                       0              0            */ out.m33 = 1;
		return out;
	}
	
}
