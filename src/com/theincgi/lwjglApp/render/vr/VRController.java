package com.theincgi.lwjglApp.render.vr;

import org.lwjgl.openvr.TrackedDevicePose;

import com.theincgi.lwjglApp.render.Drawable;

public interface VRController extends Drawable{

	void updatePoseLeft(int index, TrackedDevicePose tdp);

	void updatePoseRight(int index, TrackedDevicePose tdp);
}
