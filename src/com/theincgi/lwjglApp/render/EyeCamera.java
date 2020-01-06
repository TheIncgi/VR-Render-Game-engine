package com.theincgi.lwjglApp.render;

import org.lwjgl.openvr.HmdMatrix34;
import org.lwjgl.openvr.HmdMatrix44;
import org.lwjgl.openvr.TrackedDevicePose;
import org.lwjgl.openvr.VRSystem;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;

import org.lwjgl.util.vector.Vector4f;


import com.theincgi.lwjglApp.Utils;
import com.theincgi.lwjglApp.misc.MatrixStack;


public class EyeCamera extends Camera {
	private Matrix4f eyeOffsetLeft, eyeOffsetRight;
	
	EyeSide side = EyeSide.LEFT;
	private TrackedDevicePose hmdPos;
	
	public EyeCamera() {
		this(0,0,0);
	}
	public EyeCamera(float x, float y, float z) {
		this(x, y, z,0,0,0);
	}

	public EyeCamera(float x, float y, float z, float yaw, float pitch, float roll) {
		super(x, y, z, yaw, pitch, roll);
		near = 1.5f;
		far = 40;
	}
	
	public EyeCamera setSide(EyeSide side) {
		this.side = side;
		return this;
	}
	public EyeSide getSide() { 
		return side;
	}
	
	public EyeCamera setHmdPose(TrackedDevicePose hmdPose) {
		this.hmdPos = hmdPose;
		return this;
	}
	
	@Override
	public void loadProjectionMatrix() {
		if(eyeOffsetLeft==null) {
			HmdMatrix34 tmp = HmdMatrix34.create();
			VRSystem.VRSystem_GetEyeToHeadTransform(EyeSide.LEFT.getVal(), tmp);
			eyeOffsetLeft = Utils.fromT(tmp);
			tmp.close();
		}
		if(eyeOffsetRight==null) {
			HmdMatrix34 tmp = HmdMatrix34.create();
			VRSystem.VRSystem_GetEyeToHeadTransform(EyeSide.RIGHT.getVal(), tmp);
			eyeOffsetRight = Utils.fromT(tmp);
			tmp.close();
		}
		Matrix4f out = new Matrix4f();
		out.setIdentity();
		Matrix4f hmd = getHmdPose();
		
//VRChaperone.VRChaperone_ForceBoundsVisible(true);
		Matrix4f prj, eye;
		switch (side) {
		default:
		case LEFT:
			prj = getHMDMatrixProjectionEye(EyeSide.LEFT);
			eye = eyeOffsetLeft;
			break;
		case RIGHT:
			prj = getHMDMatrixProjectionEye(EyeSide.RIGHT);
			eye = eyeOffsetRight;
			break;
		}

		Matrix4f.mul(hmd, eye, out);
		out.invert(); //cam should do reverse rotation location compared to things like model view
		Matrix4f.mul(prj, out, out);
		
		Vector4f test = new Vector4f(0,0,-3, 1);
		Matrix4f.transform(out, test, test);
	
		MatrixStack.projection.get().load(out);
	}
	
	

	private Matrix4f hmdProjectionEyeLeft, hmdProjectEyeRight;
	@SuppressWarnings("resource") //closing the resource causes an EXCEPTION_ACCESS_VIOLATION shortly after running
	protected Matrix4f getHMDMatrixProjectionEye(EyeSide es){ 
		//try(HmdMatrix44 mat = HmdMatrix44.create()) {
			if( hmdProjectionEyeLeft == null  && es.equals(EyeSide.LEFT) ) {
				HmdMatrix44 mat = HmdMatrix44.create();
				VRSystem.VRSystem_GetProjectionMatrix(es.getVal(), near, far, mat);
				
				hmdProjectionEyeLeft = new Matrix4f();
				
	
				convertSteamVRMatrix4ToMatrix4f(mat, hmdProjectionEyeLeft);
			}
			if( hmdProjectEyeRight == null  && es.equals(EyeSide.RIGHT) ) {
				HmdMatrix44 mat = HmdMatrix44.create();
				VRSystem.VRSystem_GetProjectionMatrix(es.getVal(), near, far, mat);
				
				hmdProjectEyeRight = new Matrix4f();
				
	
				convertSteamVRMatrix4ToMatrix4f(mat, hmdProjectEyeRight);
			}
		//}
		
		return es.equals(EyeSide.LEFT)? hmdProjectionEyeLeft : hmdProjectEyeRight;
	}
	
	private Matrix4f getHmdPose() {
		return Utils.fromT(hmdPos.mDeviceToAbsoluteTracking());
	}
	
	public static Matrix4f convertSteamVRMatrix4ToMatrix4f(org.lwjgl.openvr.HmdMatrix44 hmdMatrix, Matrix4f mat){
		mat.load(hmdMatrix.m());
		return mat;
	}
}
