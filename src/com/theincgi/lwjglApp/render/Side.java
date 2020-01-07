package com.theincgi.lwjglApp.render;

import org.lwjgl.openvr.VR;

public enum Side {
	LEFT(VR.EVREye_Eye_Left), RIGHT(VR.EVREye_Eye_Right);
	
	private final int val;
	private Side(int j) {
		this.val = j;
	}
	
	public int getVal() {
		return val;
	}
	public Side other() {
		switch (this) {
		case LEFT:
			return RIGHT;
		case RIGHT:
			return LEFT;
		default:
			return null;
		}
	}
}
