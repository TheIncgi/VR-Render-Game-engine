package com.theincgi.lwjglApp.render;

import org.lwjgl.openvr.HmdMatrix34;
import org.lwjgl.openvr.HmdMatrix44;
import org.lwjgl.openvr.VR;
import org.lwjgl.openvr.VRSystem;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import com.theincgi.lwjglApp.Utils;
import com.theincgi.lwjglApp.misc.MatrixStack;

public class EyeCamera extends Camera {
	private static float eyeAngle = (float) Math.toRadians(20); 
	private Matrix4f eyeOffsetLeft, eyeOffsetRight;
	
	EyeSide side = EyeSide.LEFT;
	public EyeCamera() {
		this(0,0,0);
	}
	public EyeCamera(float x, float y, float z) {
		this(x, y, z,0,0,0);
	}

	public EyeCamera(float x, float y, float z, float yaw, float pitch, float roll) {
		super(x, y, z, yaw, pitch, roll);
		near = 1.5f;
		far = 500;
	}
	
	public EyeCamera setSide(EyeSide side) {
		this.side = side;
		return this;
	}
	public EyeSide getSide() { 
		return side;
	}
	
	@Override
	public void loadProjectionMatrix() {
		switch (side) {
		case LEFT:
			loadLeftProjectionMatrix();
			break;
		case RIGHT:
			loadRightProjectionMatrix();
			break;
		}
	}
	public void loadLeftProjectionMatrix() {
		if(eyeOffsetLeft==null) {
			HmdMatrix34 tmp = HmdMatrix34.create();
			VRSystem.VRSystem_GetEyeToHeadTransform(EyeSide.LEFT.getVal(), tmp);
			eyeOffsetLeft = Utils.fromT(tmp);
			tmp.close();
		}
		
		
		MatrixStack.projection.get().load(getHMDMatrixProjectionEye(EyeSide.LEFT));
		//location.applyTo(MatrixStack.projection.get());
		
		Matrix4f.mul( MatrixStack.projection.get(),eyeOffsetLeft, MatrixStack.projection.get());
		
		
	}
	public void loadRightProjectionMatrix() {
		if(eyeOffsetRight==null) {
			HmdMatrix34 tmp = HmdMatrix34.create();
			VRSystem.VRSystem_GetEyeToHeadTransform(EyeSide.RIGHT.getVal(), tmp);
			eyeOffsetRight = Utils.fromT(tmp);
			tmp.close();
		}
		
		MatrixStack.projection.get().load(getHMDMatrixProjectionEye(EyeSide.RIGHT));
		//location.applyTo(MatrixStack.projection.get());
		//Matrix4f.mul(MatrixStack.projection.get(), eyeOffsetRight, MatrixStack.projection.get());
	}

	private Matrix4f hmdProjectionEye;
	@SuppressWarnings("resource") //closing the resource causes an EXCEPTION_ACCESS_VIOLATION shortly after running
	public Matrix4f getHMDMatrixProjectionEye(EyeSide es){ 
		//Normally View and Eye^-1 will be multiplied
		//together and treated as View in your application.
		
		//try(HmdMatrix44 mat = HmdMatrix44.create()) {
		HmdMatrix44 mat = HmdMatrix44.create();
		
			if( hmdProjectionEye == null ) {
				VRSystem.VRSystem_GetProjectionMatrix(es.getVal(), near, far, mat);
				
				hmdProjectionEye = new Matrix4f();
				
	
				convertSteamVRMatrix4ToMatrix4f(mat, hmdProjectionEye);
			}
		//}
		return hmdProjectionEye;
	}
	
	
	
	/**
	 * Convert specific OpenVR {@link org.lwjgl.openvr.HmdMatrix34 HmdMatrix34_t} into org.lwjgl.util.vector {@link Matrix4f Matrix4f}
	 * @param hmdMatrix the input matrix
	 * @param mat the converted matrix
	 * @return Matrix4f matrix
	 */
	public static Matrix4f convertSteamVRMatrix4ToMatrix4f(org.lwjgl.openvr.HmdMatrix44 hmdMatrix, Matrix4f mat){
		mat.load(hmdMatrix.m());
		return mat;
	}
}
