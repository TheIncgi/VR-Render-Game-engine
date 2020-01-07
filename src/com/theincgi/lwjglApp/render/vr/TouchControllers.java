package com.theincgi.lwjglApp.render.vr;


import java.util.Optional;

import org.lwjgl.openvr.HmdMatrix34;
import org.lwjgl.openvr.TrackedDevicePose;
import org.lwjgl.openvr.VRControllerAxis;
import org.lwjgl.openvr.VRControllerState;
import org.lwjgl.openvr.VRSystem;
import org.lwjgl.util.vector.Vector3f;

import com.theincgi.lwjglApp.misc.MatrixStack;
import com.theincgi.lwjglApp.misc.Pair;
import com.theincgi.lwjglApp.mvc.models.Object3D;
import com.theincgi.lwjglApp.render.Drawable;
import com.theincgi.lwjglApp.render.Location;
import com.theincgi.lwjglApp.render.Model;
import com.theincgi.lwjglApp.render.ObjManager;

public class TouchControllers implements VRController {
	Location leftLocation, rightLocation;
	Optional<Model>
			 leftBase, rightBase,
	         leftAnalog, rightAnalog,
	         leftTrigger, rightTrigger,
	         leftGrip, rightGrip,
	         xButton, yButton, aButton, bButton;
	
	private static final float ANALOG_CONST = .003f; //3mm
	
	Vector3f leftAnalogVal = new Vector3f(), rightAnalogVal = new Vector3f();
	
	private static final String LEFT =  "cmodels/oculus_cv1_controller_left/oculus_cv1_controller_left_";
	private static final String RIGHT = "cmodels/oculus_cv1_controller_right/oculus_cv1_controller_right_";
	boolean leftValid, rightValid;
	
	public TouchControllers() {
		leftLocation = new Location();
		rightLocation = new Location();
		
		leftBase 		= ObjManager.INSTANCE.get(LEFT +"body.obj");
		rightBase 		= ObjManager.INSTANCE.get(RIGHT+"body.obj");
		leftAnalog 		= ObjManager.INSTANCE.get(LEFT +"thumbstick.obj");
		rightAnalog 	= ObjManager.INSTANCE.get(RIGHT+"thumbstick.obj");
		leftTrigger 	= ObjManager.INSTANCE.get(LEFT +"trigger.obj");
		rightTrigger 	= ObjManager.INSTANCE.get(RIGHT+"trigger.obj");
		leftGrip 		= ObjManager.INSTANCE.get(LEFT +"grip.obj");
		rightGrip 		= ObjManager.INSTANCE.get(RIGHT+"grip.obj");
		xButton 		= ObjManager.INSTANCE.get(LEFT +"button_x.obj");
		yButton 		= ObjManager.INSTANCE.get(LEFT +"button_y.obj");
		aButton 		= ObjManager.INSTANCE.get(RIGHT+"button_a.obj");
		bButton 		= ObjManager.INSTANCE.get(RIGHT+"button_b.obj");
	}
	
	@Override
	public void updatePoseLeft(int index, TrackedDevicePose tdp) {
		leftValid = tdp.bPoseIsValid();
		if(!leftValid) return;
		
		VRControllerState state = VRControllerState.create();
		VRSystem.VRSystem_GetControllerState(index, state);
		
		HmdMatrix34 transform = tdp.mDeviceToAbsoluteTracking();
		leftLocation.setPos(VRUtil.getPos(transform));
		
		VRControllerAxis axis = state.rAxis(0);
		leftAnalogVal.x = axis.x() * ANALOG_CONST;
		leftAnalogVal.y = axis.y() * ANALOG_CONST;
		leftValid = tdp.bPoseIsValid();
		//state.free();
	}
	
	@Override
	public void updatePoseRight(int index, TrackedDevicePose tdp) {
//		rightValid = tdp.bPoseIsValid();
//		if(!rightValid)return;
//		VRControllerState state = VRControllerState.create();
//		VRSystem.VRSystem_GetControllerState(index, state);
//		VRControllerAxis axis = state.rAxis(0);
//		rightAnalogVal.x = axis.x() * ANALOG_CONST;
//		rightAnalogVal.y = axis.y() * ANALOG_CONST;
//		
//		state.free();
	}

	@Override
	public void draw() {
		try(MatrixStack stk = MatrixStack.modelViewStack.pushTransform(leftLocation)){
			leftBase.ifPresent(lb->lb.drawAtOrigin());
			try(MatrixStack stk2 = MatrixStack.modelViewStack.pushTranslate(leftAnalogVal)){
				leftAnalog.ifPresent(la->la.drawAtOrigin());
			}
		}
	}

	
	
	
	
	
}
