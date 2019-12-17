package com.theincgi.lwjglApp;

public class Utils {
	public static boolean inRange(double x, double min, double max) {
		return min<=x && x < max;
	}
	public static boolean inRange(double x, int min, int max) {
		return min<=x && x < max;
	}
	public static boolean inRange(int x, int min, int max) {
		return min<=x && x < max;
	}
	public static boolean inRange(long x, long min, long max) {
		return min<=x && x < max;
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
}
