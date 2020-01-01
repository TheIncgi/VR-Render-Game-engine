package com.theincgi.lwjglApp.render;

import org.lwjgl.openvr.HmdMatrix44;
import org.lwjgl.openvr.VR;
import org.lwjgl.openvr.VRSystem;
import org.lwjgl.util.vector.Matrix4f;

import com.theincgi.lwjglApp.misc.MatrixStack;

public class EyeCamera extends Camera {
	private static Location localEyePosL = new Location(-.5f, 0, 0), 
					        localEyePosR = new Location( .5f, 0, 0);
	EyeSide side = EyeSide.LEFT;
	public EyeCamera() {
		super();
	}
	public EyeCamera(float x, float y, float z) {
		super(x, y, z);
	}

	public EyeCamera(float x, float y, float z, float yaw, float pitch, float roll) {
		super(x, y, z, yaw, pitch, roll);
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
		MatrixStack.projection.get().load(getHMDMatrixProjectionEye(EyeSide.LEFT));
		location.applyTo(MatrixStack.projection.get());
		localEyePosL.applyTo(MatrixStack.projection.get());
	}
	public void loadRightProjectionMatrix() {
		MatrixStack.projection.get().load(getHMDMatrixProjectionEye(EyeSide.RIGHT));
		location.applyTo(MatrixStack.projection.get());
		localEyePosR.applyTo(MatrixStack.projection.get()); //TODO check order
	}

	private Matrix4f hmdProjectionEye;
	@SuppressWarnings("resource") //closing the resource causes an EXCEPTION_ACCESS_VIOLATION shortly after running
	public Matrix4f getHMDMatrixProjectionEye(EyeSide es){
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
