package com.theincgi.lwjglApp.vrTests;

import java.lang.reflect.Field;
import java.nio.IntBuffer;

import org.lwjgl.openvr.OpenVR;
import org.lwjgl.openvr.VR;
import org.lwjgl.openvr.VRApplications;
import org.lwjgl.system.MemoryStack;


public class Test {
	
	public static void main(String[] args) {
		int vrInterface = 0;
		try(MemoryStack mem = MemoryStack.stackPush()){
			IntBuffer peError = mem.mallocInt(1);
			vrInterface = VR.VR_InitInternal(peError, VR.EVRApplicationType_VRApplication_Scene);
			System.out.println(vrInterface + " : "+peError.get(0));
			VR.VR_ShutdownInternal();
			
			discoverError(peError.get(0));
		}
		new String(Character.toChars(55));
	}
	
	private static void discoverError(int i) {
		for(Field f : VR.class.getFields()) {
			try {
				if(f.getName().toLowerCase().contains("error") &&
						java.lang.reflect.Modifier.isStatic(f.getModifiers()) &&
						f.getInt(null) == i) {
					System.out.println(f.getName());
				}
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			}
		}
	}
}
