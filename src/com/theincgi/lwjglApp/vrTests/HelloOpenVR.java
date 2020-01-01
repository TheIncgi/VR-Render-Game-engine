package com.theincgi.lwjglApp.vrTests;

/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */

import org.lwjgl.openvr.*;
import org.lwjgl.system.*;
import org.lwjgl.util.vector.Matrix4f;

import com.theincgi.lwjglApp.render.Camera;

import java.nio.*;

import static org.lwjgl.openvr.VR.*;
import static org.lwjgl.openvr.VRSystem.*;
import static org.lwjgl.system.MemoryStack.*;

public class HelloOpenVR {



	private HelloOpenVR() {
	}

	public static void main(String[] args) {
		System.out.println("VR_IsRuntimeInstalled() = " + VR_IsRuntimeInstalled());
		System.out.println("VR_RuntimePath() = " + VR_RuntimePath());
		System.out.println("VR_IsHmdPresent() = " + VR_IsHmdPresent());

		try (MemoryStack stack = stackPush()) {
			IntBuffer peError = stack.mallocInt(1);

			int token = VR_InitInternal(peError, 0);
			if (peError.get(0) == 0) {
				try {
					OpenVR.create(token);

					System.out.println("Model Number : " + VRSystem_GetStringTrackedDeviceProperty(
							k_unTrackedDeviceIndex_Hmd,
							ETrackedDeviceProperty_Prop_ModelNumber_String,
							peError
							));
					System.out.println("Serial Number: " + VRSystem_GetStringTrackedDeviceProperty(
							k_unTrackedDeviceIndex_Hmd,
							ETrackedDeviceProperty_Prop_SerialNumber_String,
							peError
							));

					IntBuffer w = stack.mallocInt(1);
					IntBuffer h = stack.mallocInt(1);
					VRSystem_GetRecommendedRenderTargetSize(w, h);
					System.out.println("Recommended width : " + w.get(0));
					System.out.println("Recommended height: " + h.get(0));


				} finally {
					VR_ShutdownInternal();
				}
			} else {
				System.err.println("INIT ERROR SYMBOL: " + VR_GetVRInitErrorAsSymbol(peError.get(0)));
				System.err.println("INIT ERROR  DESCR: " + VR_GetVRInitErrorAsEnglishDescription(peError.get(0)));
			}
		}
	}

	
}