package com.theincgi.lwjglApp.render.vr;

import static org.lwjgl.openvr.VR.ETrackedDeviceProperty_Prop_ModelNumber_String;
import static org.lwjgl.openvr.VR.ETrackedDeviceProperty_Prop_SerialNumber_String;
import static org.lwjgl.openvr.VR.VR_GetVRInitErrorAsEnglishDescription;
import static org.lwjgl.openvr.VR.VR_GetVRInitErrorAsSymbol;
import static org.lwjgl.openvr.VR.VR_InitInternal;
import static org.lwjgl.openvr.VR.VR_IsHmdPresent;
import static org.lwjgl.openvr.VR.VR_IsRuntimeInstalled;
import static org.lwjgl.openvr.VR.VR_RuntimePath;
import static org.lwjgl.openvr.VR.VR_ShutdownInternal;
import static org.lwjgl.openvr.VR.k_unTrackedDeviceIndex_Hmd;
import static org.lwjgl.openvr.VRSystem.VRSystem_GetRecommendedRenderTargetSize;
import static org.lwjgl.openvr.VRSystem.VRSystem_GetStringTrackedDeviceProperty;
import static org.lwjgl.system.MemoryStack.stackPush;
import static java.lang.Math.max;
import static java.lang.Math.copySign;
import static java.lang.Math.sqrt;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.nio.IntBuffer;
import java.util.StringJoiner;
import java.util.WeakHashMap;

import org.lwjgl.openvr.HmdMatrix34;
import org.lwjgl.openvr.OpenVR;
import org.lwjgl.openvr.Texture;
import org.lwjgl.openvr.VR;
import org.lwjgl.openvr.VRApplications;
import org.lwjgl.openvr.VRCompositor;
import org.lwjgl.openvr.VRInput;
import org.lwjgl.openvr.VRSystem;
import org.lwjgl.openvr.VRTextureBounds;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.theincgi.lwjglApp.misc.Logger;

public class VRUtil implements AutoCloseable, Closeable {
	private Texture leftTexture, rightTexture;
	int colorSpace = VR.EColorSpace_ColorSpace_Linear;
	
	public VRUtil() {
	}

	private int vrToken, width, height;
	private String MODEL, SERIAL;
	public void initVR() {
		System.out.println("VR_IsRuntimeInstalled() = " + VR_IsRuntimeInstalled());
		System.out.println("VR_RuntimePath() = " + VR_RuntimePath());
		System.out.println("VR_IsHmdPresent() = " + VR_IsHmdPresent());

		try (MemoryStack stack = stackPush()) {
			IntBuffer peError = stack.mallocInt(1);

			vrToken = VR_InitInternal(peError, VR.EVRApplicationType_VRApplication_Scene);
			if (peError.get(0) == 0) {
				Logger.preferedLogger.i("VRUtil#initVR", "VR Token: "+vrToken);
				OpenVR.create(vrToken);
				MODEL = VRSystem_GetStringTrackedDeviceProperty(
						k_unTrackedDeviceIndex_Hmd,
						ETrackedDeviceProperty_Prop_ModelNumber_String,
						peError
						);
				SERIAL = VRSystem_GetStringTrackedDeviceProperty(
						k_unTrackedDeviceIndex_Hmd,
						ETrackedDeviceProperty_Prop_SerialNumber_String,
						peError
						);
				IntBuffer w = stack.mallocInt(1);
				IntBuffer h = stack.mallocInt(1);
				VRSystem_GetRecommendedRenderTargetSize(w, h);
				width = w.get(0);
				height = h.get(0);
				Logger.preferedLogger.i("VRUtil#initVR", String.format("Recommended render target size: <%d, %d>", width, height));
				
			} else {
				Logger.preferedLogger.e("VRUtil#initVR", new VRException(peError.get(0)));
			}
		}
		
//		VRCompositor.VRCompositor_ShowMirrorWindow();
		
	}
	
	public void bindEyeTextures(long left, long right, Boolean isGammaEncoded) {
		leftTexture = Texture.create();
		leftTexture.set(left, VR.ETextureType_TextureType_OpenGL, isGammaEncoded?VR.EColorSpace_ColorSpace_Auto : isGammaEncoded?VR.EColorSpace_ColorSpace_Gamma:VR.EColorSpace_ColorSpace_Linear);
		
		rightTexture = Texture.create();
		rightTexture.set(right,VR.ETextureType_TextureType_OpenGL, isGammaEncoded?VR.EColorSpace_ColorSpace_Auto :  isGammaEncoded?VR.EColorSpace_ColorSpace_Gamma : VR.EColorSpace_ColorSpace_Linear);
		flags = VR.EVRSubmitFlags_Submit_Default;
	}
	public void bindEyeBuffers(long left, long right, Boolean isGammaEncoded) {
		leftTexture = Texture.create();
		leftTexture.set(left, VR.ETextureType_TextureType_OpenGL, isGammaEncoded?VR.EColorSpace_ColorSpace_Auto : isGammaEncoded?VR.EColorSpace_ColorSpace_Gamma:VR.EColorSpace_ColorSpace_Linear);
		
		rightTexture = Texture.create();
		rightTexture.set(right,VR.ETextureType_TextureType_OpenGL, isGammaEncoded?VR.EColorSpace_ColorSpace_Auto :  isGammaEncoded?VR.EColorSpace_ColorSpace_Gamma : VR.EColorSpace_ColorSpace_Linear);
		flags = VR.EVRSubmitFlags_Submit_GlRenderBuffer;
	}
	private int flags = 0; //==default
	public void submitFrame() {
		//int flags = VR.EVRSubmitFlags_Submit_Default ;//| VR.EVRSubmitFlags_Submit_GlRenderBuffer; TextureUsesUnsupportedFormat caused by this if using a texture an not a render buffer, oops
		int code = 0;
		code = VRCompositor.VRCompositor_Submit(VR.EVREye_Eye_Left,  leftTexture,  null, flags);
		switch (code) {
		case VR.EVRCompositorError_VRCompositorError_IsNotSceneApplication:
			Logger.preferedLogger.w("VRUtil#submitFrame", "IsNotSceneApplication");
			break;
		case VR.EVRCompositorError_VRCompositorError_AlreadySubmitted:
			Logger.preferedLogger.w("VRUtil#submitFrame", "AlreadySubmitted"); break;
		case VR.EVRCompositorError_VRCompositorError_DoNotHaveFocus:
			Logger.preferedLogger.w("VRUtil#submitFrame", "DoNotHaveFocus"); break;
		case VR.EVRCompositorError_VRCompositorError_IncompatibleVersion:
			Logger.preferedLogger.w("VRUtil#submitFrame", "IncompatibleVersion"); break;
		case VR.EVRCompositorError_VRCompositorError_IndexOutOfRange:
			Logger.preferedLogger.w("VRUtil#submitFrame", "IndexOutOfRange"); break;
		case VR.EVRCompositorError_VRCompositorError_InvalidBounds:
			Logger.preferedLogger.w("VRUtil#submitFrame", "InvalidBounds"); break;
		case VR.EVRCompositorError_VRCompositorError_InvalidTexture:
			Logger.preferedLogger.w("VRUtil#submitFrame", "InvalidTexture"); break;
		case VR.EVRCompositorError_VRCompositorError_None:
			break;
		case VR.EVRCompositorError_VRCompositorError_RequestFailed:
			Logger.preferedLogger.w("VRUtil#submitFrame", "RequestFailed"); break;
		case VR.EVRCompositorError_VRCompositorError_SharedTexturesNotSupported:
			Logger.preferedLogger.w("VRUtil#submitFrame", "SharedTexturesNotSupported"); break;
		case VR.EVRCompositorError_VRCompositorError_TextureIsOnWrongDevice:
			Logger.preferedLogger.w("VRUtil#submitFrame", "TextureIsOnWrongDevice"); break;
		case VR.EVRCompositorError_VRCompositorError_TextureUsesUnsupportedFormat:
			Logger.preferedLogger.w("VRUtil#submitFrame", "TextureUsesUnsupportedFormat"); break;
		default:
			break;
		}
		code = VRCompositor.VRCompositor_Submit(VR.EVREye_Eye_Right, rightTexture, null, flags);
		
		VRCompositor.VRCompositor_PostPresentHandoff();
		
	}
	
	private static WeakHashMap<Integer, String> eventNameLookup = new WeakHashMap<>();
	public static String getEventName(int ecode) {
		return eventNameLookup.computeIfAbsent(ecode, (k)->{
			try {
				StringJoiner out = new StringJoiner(" | ");
				for(Field f : VR.class.getFields()) {
					if(java.lang.reflect.Modifier.isStatic(f.getModifiers()))
						if(f.getType().equals(Integer.TYPE))
							if(f.getInt(null) == ecode && f.getName().startsWith("EVREventType_VREvent_")) out.add(f.getName());
				}
				return out.length()==0?"NOT FOUND ("+ecode+")" : out.toString().replace("EVREventType_VREvent_", "");
			} catch (Exception ex ) {
				return "?";
			}
		});
	}
	
		/* Enum values:
•EVRSubmitFlags_Submit_Default                       Simple render path. App submits rendered left and right eye images with no lens distortion correction applied.
•EVRSubmitFlags_Submit_LensDistortionAlreadyApplied  App submits final left and right eye images with lens distortion already applied (lens distortion makes the images appear barrel distorted withchromatic aberration correction applied). The app would have used the data returned by ComputeDistortion to apply the correct distortion to therendered images before calling Submit. 
•EVRSubmitFlags_Submit_GlRenderBuffer                If the texture pointer passed in is actually a renderbuffer (e.g. for MSAA in OpenGL) then set this flag.
•EVRSubmit_TextureWithPose                           Set to indicate that pTexture is a pointer to a VRTextureWithPose. 
                                                     This flag can be combined with EVRSubmitFlags_Submit_TextureWithDepth to pass a VRTextureWithPoseAndDepth.

•EVRSubmitFlags_Submit_TextureWithDepth -Set to indicate that pTexture is a pointer to a VRTextureWithDepth. 
This flag can be combined with EVRSubmit_TextureWithPose to pass a VRTextureWithPoseAndDepth.


		 * 
		 * C++
		 *  Call immediately before OpenGL swap buffers 
			void submitToHMD(GLint ltEyeTexture, GLint rtEyeTexture, bool isGammaEncoded) {
		    const vr::EColorSpace colorSpace = isGammaEncoded ? vr::ColorSpace_Gamma : vr::ColorSpace_Linear;
		
		    const vr::Texture_t lt = { reinterpret_cast<void*>(intptr_t(ltEyeTexture)), vr::TextureType_OpenGL, colorSpace };
		    vr::VRCompositor()->Submit(vr::Eye_Left, &lt);
		
		    const vr::Texture_t rt = { reinterpret_cast<void*>(intptr_t(rtEyeTexture)), vr::TextureType_OpenGL, colorSpace };
		    vr::VRCompositor()->Submit(vr::Eye_Right, &rt);
		
		    // Tell the compositor to begin work immediately instead of waiting for the next WaitGetPoses() call
		    vr::VRCompositor()->PostPresentHandoff();
}
		 * */
	
	
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	public String getMODEL() {
		return MODEL;
	}
	public String getSERIAL() {
		return SERIAL;
	}

	/**Calls VR_Shutd*/
	@Override public void close() {
		VR_ShutdownInternal();
		if(leftTexture!=null)leftTexture.close();
		if(rightTexture!=null)rightTexture.close();
	}


	//chose Exception over RuntimeException to encourage proper error handling
	public static class VRException extends Exception {
		public VRException(int code) {
			super(String.format("VR Err: [%s] %s", VR_GetVRInitErrorAsSymbol(code), VR_GetVRInitErrorAsEnglishDescription(code)));
		}
	}
	
	//https://github.com/osudrl/OpenVR-Tracking-Example/blob/a6753ba7bf77715372a60bcf2bc0232e0d9542e3/LighthouseTracking.cpp#L277-L286
	static Vector3f getPos(HmdMatrix34 matrix) 
	{
		return new Vector3f(matrix.m(3), matrix.m(7), matrix.m(11));
	}

	public static Vector4f getRotation(HmdMatrix34 matrix)
	{
		Vector4f q = new Vector4f();
		float m00 = matrix.m(0);
		float m11 = matrix.m(5);
		float m22 = matrix.m(10);
		float m21 = matrix.m(6);
		float m02 = matrix.m(8);
		float m10 = matrix.m(1);
		float m12 = matrix.m(9);
		float m20 = matrix.m(2);
		float m01 = matrix.m(4);
		q.w = (float) (sqrt(max(0, 1 + m00 + m11 + m22)) / 2);
		q.x = (float) (sqrt(max(0, 1 + m00 - m11 - m22)) / 2);
		q.y = (float) (sqrt(max(0, 1 - m00 + m11 - m22)) / 2);
		q.z = (float) (sqrt(max(0, 1 - m00 - m11 + m22)) / 2);
		
		q.x = copySign(q.x, m21 - m12); //left gets sign of right
		q.y = copySign(q.y, m02 - m20);
		q.z = copySign(q.z, m10 - m01);
		return q;
	}
	
}
