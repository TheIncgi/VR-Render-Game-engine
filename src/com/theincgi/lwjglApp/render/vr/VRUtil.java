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

import java.io.Closeable;
import java.nio.IntBuffer;

import org.lwjgl.openvr.OpenVR;
import org.lwjgl.openvr.Texture;
import org.lwjgl.openvr.VRCompositor;
import org.lwjgl.system.MemoryStack;

import com.theincgi.lwjglApp.misc.Logger;

public class VRUtil implements AutoCloseable, Closeable {

	
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

			vrToken = VR_InitInternal(peError, 0);
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
	}
	
	
	
	public void submitFrame() {
		Texture tex;
		
		//VRCompositor.VRCompositor_Submit(eEye, pTexture, pBounds, nSubmitFlags)
	
		/* C++
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
	}
	
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
	}


	//chose Exception over RuntimeException to encourage proper error handling
	public static class VRException extends Exception {
		public VRException(int code) {
			super(String.format("VR Err: [%s] %s", VR_GetVRInitErrorAsSymbol(code), VR_GetVRInitErrorAsEnglishDescription(code)));
		}
	}
}
