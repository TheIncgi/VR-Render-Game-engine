package com.theincgi.lwjglApp.render;

import org.lwjgl.openvr.HmdMatrix44;
import org.lwjgl.openvr.VR;
import org.lwjgl.openvr.VRSystem;
import org.lwjgl.util.vector.Matrix4f;

import com.theincgi.lwjglApp.misc.MatrixStack;

public class EyeCamera extends Camera {
	private EyeSide eyeSide = EyeSide.LEFT;
	
	public EyeCamera() {
		super();
	}
	public EyeCamera(float x, float y, float z) {
		super(x, y, z);
	}

	public EyeCamera(float x, float y, float z, float yaw, float pitch, float roll) {
		super(x, y, z, yaw, pitch, roll);
	}
	
	/**Defaults to left if not set*/
	public EyeCamera setEyeSide(EyeSide side){
		this.eyeSide  = side;
		return this;
	}
	public EyeSide getEyeSide() {
		return eyeSide;
	}

	@Override
	public void loadProjectionMatrix() {
		MatrixStack.projection.get().load(getHMDMatrixProjectionLeftEye());
		location.applyTo(MatrixStack.projection.get());
	}

	private Matrix4f hmdProjectionEye;
	@SuppressWarnings("resource") //closing the resource causes an EXCEPTION_ACCESS_VIOLATION shortly after running
	public Matrix4f getHMDMatrixProjectionLeftEye(){
		//try(HmdMatrix44 mat = HmdMatrix44.create()) {
		HmdMatrix44 mat = HmdMatrix44.create();
			if( hmdProjectionEye == null ) {
				VRSystem.VRSystem_GetProjectionMatrix(eyeSide.getVal(), near, far, mat);
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
