package com.theincgi.lwjglApp.render;

import java.nio.FloatBuffer;

import org.lwjgl.openvr.HmdMatrix34;
import org.lwjgl.openvr.HmdMatrix44;
import org.lwjgl.openvr.TrackedDevicePose;
import org.lwjgl.openvr.VRSystem;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;


import com.theincgi.lwjglApp.Utils;
import com.theincgi.lwjglApp.misc.MatrixStack;
import com.theincgi.lwjglApp.render.shaders.ShaderProgram;


public class EyeCamera extends Camera {
	private Matrix4f eyeOffset;

	Side side = Side.LEFT;
	private TrackedDevicePose hmdPos;

	public EyeCamera() {
		this(0,0,0);
	}
	public EyeCamera(float x, float y, float z) {
		this(x, y, z,0,0,0);
	}

	public EyeCamera(float x, float y, float z, float yaw, float pitch, float roll) {
		super(x, y, z, yaw, pitch, roll);
		near = .1f;
		far = 1010f;
	}

	public EyeCamera setSide(Side side) {
		this.side = side;
		hmdProjection = null;
		eyeOffset = null;
		return this;
	}
	public Side getSide() { 
		return side;
	}

	public EyeCamera setHmdPose(TrackedDevicePose hmdPose) {
		this.hmdPos = hmdPose;
		return this;
	}

	public void tellShader(ShaderProgram sp) {
		if(sp!=null && hmdProjection!=null) {
			Matrix4f m = MatrixStack.view.get();
			sp.trySetUniform("cameraPos", new float[] {-m.m30, -m.m31, -m.m32});
		}
	}

	@Override
	public Matrix4f projectionMatrix() {
		return getHMDMatrixProjectionEye();
	}

	@Override
	public void viewMatrix() {
		HmdMatrix34 tmp = HmdMatrix34.create();
		VRSystem.VRSystem_GetEyeToHeadTransform(side.getVal(), tmp);
		eyeOffset = Utils.fromT(tmp);
		//tmp.close();

		Matrix4f out = new Matrix4f();
		out.setIdentity();
		Matrix4f hmd = getHmdPose();

		//VRChaperone.VRChaperone_ForceBoundsVisible(true);

		Matrix4f.mul(hmd, eyeOffset, out);
		out.invert(); //cam should do reverse rotation location compared to things like model view
		//Matrix4f.mul(prj, out, out);

		MatrixStack.view.get().load(out);
	}



	private Matrix4f hmdProjection;
	@SuppressWarnings("resource") //closing the resource causes an EXCEPTION_ACCESS_VIOLATION shortly after running
	protected Matrix4f getHMDMatrixProjectionEye(){ 
		//TODO Special thanks to this poster
		//https://github.com/ValveSoftware/openvr/issues/1052
		//saved my project
		if(hmdProjection == null)
			try(MemoryStack ms = MemoryStack.stackPush()){
				FloatBuffer left, right, top, bottom;
				left   = ms.mallocFloat(1);
				right  = ms.mallocFloat(1);
				top    = ms.mallocFloat(1);
				bottom = ms.mallocFloat(1);
				VRSystem.VRSystem_GetProjectionRaw(side.getVal(), left, right, top, bottom);

				hmdProjection = ComposeProjection(left.get(0), right.get(0), top.get(0), bottom.get(0), near, far);
			}
		return hmdProjection;
	}

	protected static Matrix4f ComposeProjection(float fLeft, float fRight, float fTop, float fBottom, float zNear, float zFar)
	{
		float idx = 1.0f / (fRight - fLeft);
		float idy = 1.0f / (fBottom - fTop);
		float idz = 1.0f / (zFar - zNear);
		float sx = fRight + fLeft;
		float sy = fBottom + fTop;

		Matrix4f p = new Matrix4f();
		p.m00 = 2*idx;   p.m10  = 0;     p.m20 = sx*idx;    			p.m30 = 0;
		p.m01 = 0;       p.m11  = 2*idy; p.m21 = sy*idy;    			p.m31 = 0;
		p.m02 = 0;       p.m12  = 0;     p.m22 = -(zFar + zNear)*idz;	p.m32 = -2*zFar*zNear*idz;
		p.m03 = 0;       p.m13 = 0;      p.m23 = -1.0f;     			p.m33 = 0;
		return p;
	}


	private Matrix4f getHmdPose() {
		return Utils.fromT(hmdPos.mDeviceToAbsoluteTracking());
	}

	public static Matrix4f convertSteamVRMatrix4ToMatrix4f(org.lwjgl.openvr.HmdMatrix44 hmdMatrix, Matrix4f mat){
		mat.load(hmdMatrix.m());
		return mat;
	}
}
