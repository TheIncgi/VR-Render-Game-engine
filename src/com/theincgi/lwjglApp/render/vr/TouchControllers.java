package com.theincgi.lwjglApp.render.vr;


import java.util.Optional;

import org.lwjgl.openvr.HmdMatrix34;
import org.lwjgl.openvr.TrackedDevicePose;
import org.lwjgl.openvr.VR;
import org.lwjgl.openvr.VRControllerAxis;
import org.lwjgl.openvr.VRControllerState;
import org.lwjgl.openvr.VRSystem;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.theincgi.lwjglApp.Utils;
import com.theincgi.lwjglApp.misc.MatrixStack;
import com.theincgi.lwjglApp.render.Location;
import com.theincgi.lwjglApp.render.Model;
import com.theincgi.lwjglApp.render.ObjManager;
import com.theincgi.lwjglApp.render.Side;
import com.theincgi.lwjglApp.render.vr.VRController.Type;

public class TouchControllers implements VRController {
	public static final VRController.Input
		A_BUTTON 	   = new Input(Type.OCULUS_TOUCH, Side.RIGHT, false, 7),
		B_BUTTON       = new Input(Type.OCULUS_TOUCH, Side.RIGHT, false, 1),
		X_BUTTON 	   = new Input(Type.OCULUS_TOUCH, Side.LEFT, false, 7),
		Y_BUTTON       = new Input(Type.OCULUS_TOUCH, Side.LEFT, false, 1),
		LTHUMB_BUTTON  = new Input(Type.OCULUS_TOUCH, Side.LEFT, false, 32),
		RTHUMB_BUTTON  = new Input(Type.OCULUS_TOUCH, Side.RIGHT, false, 32),
		LTRIGGER_BUTTON= new Input(Type.OCULUS_TOUCH, Side.LEFT, false, 33),
		RTRIGGER_BUTTON= new Input(Type.OCULUS_TOUCH, Side.RIGHT, false, 33),
		LGRIP = new Input(Type.OCULUS_TOUCH, Side.LEFT, false,  2),
		RGRIP = new Input(Type.OCULUS_TOUCH, Side.RIGHT, false, 2);
	
	Location leftLocation, rightLocation;
	Matrix4f leftTransform, rightTransform;
	Optional<Model>
			 leftBase, rightBase,
	         leftAnalog, rightAnalog,
	         leftTrigger, rightTrigger,
	         leftGrip, rightGrip,
	         xButton, yButton, aButton, bButton, enter, home;
	
	
	Vector3f leftAnalogVal = new Vector3f(), rightAnalogVal = new Vector3f();
	boolean isAPressed, isBPressed, isXPressed, isYPressed, isLeftAnalogPressed, isRightAnalogPressed, isLeftGripPressed, isRightGripPressed, isLeftTriggerPressed, isRightTriggerPressed;
	boolean isATouch, 	isBTouch, 	isXTouch, 	isYTouch, 	isLeftAnalogTouch, 	 isRightAnalogTouch,                                          isLeftTriggerTouch,   isRightTriggerTouch;
	
	public static final String shaderName = "full";
	
	float leftTriggerAmount, rightTriggerAmount,
	      leftGripAmount, rightGripAmount;
	
	private static final String LEFT =  "cmodels/oculus_cv1_controller_left/oculus_cv1_controller_left_";
	private static final String RIGHT = "cmodels/oculus_cv1_controller_right/oculus_cv1_controller_right_";
	boolean leftValid, rightValid;
	
	public TouchControllers() {
		leftLocation = new Location();
		rightLocation = new Location();
		leftTransform = new Matrix4f();
		rightTransform = new Matrix4f();
		
		leftBase 		= ObjManager.INSTANCE.get(LEFT +"body.obj", 		shaderName);
		rightBase 		= ObjManager.INSTANCE.get(RIGHT+"body.obj", 		shaderName);
		leftAnalog 		= ObjManager.INSTANCE.get(LEFT +"thumbstick.obj", 	shaderName);
		rightAnalog 	= ObjManager.INSTANCE.get(RIGHT+"thumbstick.obj", 	shaderName);
		leftTrigger 	= ObjManager.INSTANCE.get(LEFT +"trigger.obj", 		shaderName);
		rightTrigger 	= ObjManager.INSTANCE.get(RIGHT+"trigger.obj", 		shaderName);
		leftGrip 		= ObjManager.INSTANCE.get(LEFT +"grip.obj", 		shaderName);
		rightGrip 		= ObjManager.INSTANCE.get(RIGHT+"grip.obj", 		shaderName);
		xButton 		= ObjManager.INSTANCE.get(LEFT +"button_x.obj", 	shaderName);
		yButton 		= ObjManager.INSTANCE.get(LEFT +"button_y.obj", 	shaderName);
		aButton 		= ObjManager.INSTANCE.get(RIGHT+"button_a.obj", 	shaderName);
		bButton 		= ObjManager.INSTANCE.get(RIGHT+"button_b.obj", 	shaderName);
		enter 			= ObjManager.INSTANCE.get(LEFT+ "button_enter.obj", shaderName);
		home 			= ObjManager.INSTANCE.get(RIGHT+ "button_home.obj", shaderName);
	}
	
	@Override
	public void updatePoseLeft(int index, TrackedDevicePose tdp) {
		leftValid = tdp.bPoseIsValid();
		if(!leftValid) return;
		
		VRControllerState state = VRControllerState.create();
		VRSystem.VRSystem_GetControllerState(index, state);
		
		Utils.fromT(tdp.mDeviceToAbsoluteTracking(), leftTransform);
		leftLocation.setPos(VRUtil.getPos(leftTransform));
		//leftLocation.rot[1] = (float) Math.toDegrees(Math.atan2(transform.m21, transform.m22));
		//leftLocation.rot[1] = (float) Math.toDegrees(Math.acos(transform.m22));
		//leftLocation.rot[2] = (float) Math.toDegrees(-Math.atan2(transform.m02, transform.m12));
		
		VRControllerAxis axis = state.rAxis(0); 
		leftAnalogVal.x = axis.x();
		leftAnalogVal.y = axis.y();
		
		
		leftTriggerAmount = state.rAxis(1).x();
		leftGripAmount    = state.rAxis(2).x(); 
		
		
		{long buttonPresses = state.ulButtonPressed();
			isXPressed = (buttonPresses & 0b10000000) > 0;
			isYPressed = (buttonPresses & 0b10) > 0;
			isLeftAnalogPressed = (buttonPresses & 0b100000000000000000000000000000000l) > 0; // 1<<32
			isLeftGripPressed = (buttonPresses & 0b100) > 0; //10000000000000000000000000000000100  2 flags?
			isLeftTriggerPressed = (buttonPresses & 0b1000000000000000000000000000000000l) > 0;
		}
		{long buttonTouch = state.ulButtonTouched();
			isXTouch = (buttonTouch & 0b10000000) > 0;
			isYTouch = (buttonTouch & 0b10) > 0;
			isLeftAnalogTouch = (buttonTouch & 0b100000000000000000000000000000000l) > 0; // 1<<32
			isLeftTriggerTouch = (buttonTouch & 0b1000000000000000000000000000000000l) > 0;
		}
		
		
		leftValid = tdp.bPoseIsValid();
		//state.free();
	}
	
	@Override
	public void updatePoseRight(int index, TrackedDevicePose tdp) {
		rightValid = tdp.bPoseIsValid();
		if(!rightValid) return;
		
		VRControllerState state = VRControllerState.create();
		VRSystem.VRSystem_GetControllerState(index, state);
		
		Utils.fromT(tdp.mDeviceToAbsoluteTracking(), rightTransform);
		rightLocation.setPos(VRUtil.getPos(rightTransform));
		//leftLocation.rot[1] = (float) Math.toDegrees(Math.atan2(transform.m21, transform.m22));
		//leftLocation.rot[1] = (float) Math.toDegrees(Math.acos(transform.m22));
		//leftLocation.rot[2] = (float) Math.toDegrees(-Math.atan2(transform.m02, transform.m12));
		
		VRControllerAxis axis = state.rAxis(0); 
		rightAnalogVal.x = axis.x();
		rightAnalogVal.y = axis.y();
		
		
		rightTriggerAmount = state.rAxis(1).x();
		rightGripAmount    = state.rAxis(2).x(); 
		
		
		{long buttonPresses = state.ulButtonPressed();
			isAPressed = (buttonPresses & 128) > 0;
			isBPressed = (buttonPresses & 2) > 0;
			isRightAnalogPressed = (buttonPresses & 0b100000000000000000000000000000000l) > 0; // 1<<32
			isRightGripPressed = (buttonPresses & 0b100) > 0; //10000000000000000000000000000000100  2 flags?
			isRightTriggerPressed = (buttonPresses & 0b1000000000000000000000000000000000l) > 0;
		}
		{long buttonTouch = state.ulButtonTouched();
			isATouch = (buttonTouch & 128) > 0;
			isBTouch = (buttonTouch & 2) > 0;
			isRightAnalogTouch = (buttonTouch & 0b100000000000000000000000000000000l) > 0; // 1<<32
			isRightTriggerTouch = (buttonTouch & 0b1000000000000000000000000000000000l) > 0;
		}
		
		
		rightValid = tdp.bPoseIsValid();
		//state.free();
	}

	private static final Vector3f AXIS_REL_FOW = (Vector3f) new Vector3f(0, -1, -1).normalize();
	private static final Vector3f AXIS_BUTTON  = (Vector3f) new Vector3f(0, -1, 1).normalize().scale(.0015f);
	private static final Vector3f AXIS_GRIP_LEFT = (Vector3f) new Vector3f(-1, 0, 0).scale(.005f);
	private static final Vector3f AXIS_GRIP_RIGHT = (Vector3f) new Vector3f(1, 0, 0).scale(.005f);
	@Override
	public void draw() {
		//the left
		
		try(MatrixStack stk = MatrixStack.modelViewStack.pushTransform(leftTransform)){
			leftBase.ifPresent(lb->lb.drawAtOrigin());
			{float tx=0, ty=-0.014f, tz=0.0539f, maxAngle = 20;
			try(MatrixStack stk2 = MatrixStack.modelViewStack.push()){ //.05 .01
				stk2.get().translate(new Vector3f(tx, ty, tz));
				stk2.get().rotateDeg(leftAnalogVal.y*-maxAngle, Utils.AXIS_RIGHT);
				stk2.get().rotateDeg(leftAnalogVal.x*maxAngle, AXIS_REL_FOW);
				stk2.get().translate(new Vector3f(-tx, -ty, -tz));
				if(isLeftAnalogPressed) {
					stk2.get().translate(AXIS_BUTTON);
				}
				leftAnalog.ifPresent(la->la.drawAtOrigin());
			}}
			{float tx=0, ty=-0.03f, tz=0.032f, maxAngle = 18;
			try(MatrixStack stk2 = MatrixStack.modelViewStack.push()){ //.05 .01
				stk2.get().translate(new Vector3f(tx, ty, tz));
				stk2.get().rotateDeg(leftTriggerAmount*-maxAngle, Utils.AXIS_RIGHT);
				stk2.get().translate(new Vector3f(-tx, -ty, -tz));
				leftTrigger.ifPresent(la->la.drawAtOrigin());
			}}
			if(isXPressed) {
				try(MatrixStack stk2 = MatrixStack.modelViewStack.pushTranslate(AXIS_BUTTON)){
					xButton.ifPresent(x->x.drawAtOrigin());}
			}else {
				xButton.ifPresent(x->x.drawAtOrigin());
			}
			if(isYPressed) {
				try(MatrixStack stk2 = MatrixStack.modelViewStack.pushTranslate(AXIS_BUTTON)){
					yButton.ifPresent(x->x.drawAtOrigin());}
			}else {
				yButton.ifPresent(x->x.drawAtOrigin());
			}
			
			try(MatrixStack stk2 = MatrixStack.modelViewStack.pushTranslate((Vector3f) new Vector3f(AXIS_GRIP_LEFT).scale(leftGripAmount))){
					leftGrip.ifPresent(x->x.drawAtOrigin());}
			
			enter.ifPresent(e->e.drawAtOrigin());
		}
		
		//and the right
		
		try(MatrixStack stk = MatrixStack.modelViewStack.pushTransform(rightTransform)){
			rightBase.ifPresent(lb->lb.drawAtOrigin());
			{float tx=0, ty=-0.014f, tz=0.0539f, maxAngle = 20;
			try(MatrixStack stk2 = MatrixStack.modelViewStack.push()){ //.05 .01
				stk2.get().translate(new Vector3f(tx, ty, tz));
				stk2.get().rotateDeg(rightAnalogVal.y*-maxAngle, Utils.AXIS_RIGHT);
				stk2.get().rotateDeg(rightAnalogVal.x*maxAngle, AXIS_REL_FOW);
				stk2.get().translate(new Vector3f(-tx, -ty, -tz));
				if(isRightAnalogPressed) {
					stk2.get().translate(AXIS_BUTTON);
				}
				rightAnalog.ifPresent(la->la.drawAtOrigin());
			}}
			{float tx=0, ty=-0.03f, tz=0.032f, maxAngle = 18;
			try(MatrixStack stk2 = MatrixStack.modelViewStack.push()){ //.05 .01
				stk2.get().translate(new Vector3f(tx, ty, tz));
				stk2.get().rotateDeg(rightTriggerAmount*-maxAngle, Utils.AXIS_RIGHT);
				stk2.get().translate(new Vector3f(-tx, -ty, -tz));
				rightTrigger.ifPresent(la->la.drawAtOrigin());
			}}
			if(isAPressed) {
				try(MatrixStack stk2 = MatrixStack.modelViewStack.pushTranslate(AXIS_BUTTON)){
					aButton.ifPresent(x->x.drawAtOrigin());}
			}else {
				aButton.ifPresent(x->x.drawAtOrigin());
			}
			if(isBPressed) {
				try(MatrixStack stk2 = MatrixStack.modelViewStack.pushTranslate(AXIS_BUTTON)){
					bButton.ifPresent(x->x.drawAtOrigin());}
			}else {
				bButton.ifPresent(x->x.drawAtOrigin());
			}
			
			try(MatrixStack stk2 = MatrixStack.modelViewStack.pushTranslate((Vector3f) new Vector3f(AXIS_GRIP_RIGHT).scale(rightGripAmount))){
				rightGrip.ifPresent(x->x.drawAtOrigin());}
			
			home.ifPresent(h->h.drawAtOrigin());
		}
		
	}

	
	public Location getLeftLocation() {
		return leftLocation;
	}
	
	public Location getRightLocation() {
		return rightLocation;
	}
	@Override
	public Matrix4f getLeftTransform() {
		return leftTransform;
	}
	@Override
	public Matrix4f getRightTransform() {
		return rightTransform;
	}

	public Optional<Model> getLeftBase() {
		return leftBase;
	}

	public Optional<Model> getRightBase() {
		return rightBase;
	}

	public Optional<Model> getLeftAnalog() {
		return leftAnalog;
	}

	public Optional<Model> getRightAnalog() {
		return rightAnalog;
	}

	public Optional<Model> getLeftTrigger() {
		return leftTrigger;
	}

	public Optional<Model> getRightTrigger() {
		return rightTrigger;
	}

	public Optional<Model> getLeftGrip() {
		return leftGrip;
	}

	public Optional<Model> getRightGrip() {
		return rightGrip;
	}

	public Optional<Model> getxButton() {
		return xButton;
	}

	public Optional<Model> getyButton() {
		return yButton;
	}

	public Optional<Model> getaButton() {
		return aButton;
	}

	public Optional<Model> getbButton() {
		return bButton;
	}

	public Optional<Model> getEnter() {
		return enter;
	}

	public Optional<Model> getHome() {
		return home;
	}

	public Vector3f getLeftAnalogVal() {
		return leftAnalogVal;
	}

	public Vector3f getRightAnalogVal() {
		return rightAnalogVal;
	}

	public boolean isAPressed() {
		return isAPressed;
	}

	public boolean isBPressed() {
		return isBPressed;
	}

	public boolean isXPressed() {
		return isXPressed;
	}

	public boolean isYPressed() {
		return isYPressed;
	}

	public boolean isLeftAnalogPressed() {
		return isLeftAnalogPressed;
	}

	public boolean isRightAnalogPressed() {
		return isRightAnalogPressed;
	}

	public boolean isLeftGripPressed() {
		return isLeftGripPressed;
	}

	public boolean isRightGripPressed() {
		return isRightGripPressed;
	}

	public boolean isLeftTriggerPressed() {
		return isLeftTriggerPressed;
	}

	public boolean isRightTriggerPressed() {
		return isRightTriggerPressed;
	}

	public boolean isATouch() {
		return isATouch;
	}

	public boolean isBTouch() {
		return isBTouch;
	}

	public boolean isXTouch() {
		return isXTouch;
	}

	public boolean isYTouch() {
		return isYTouch;
	}

	public boolean isLeftAnalogTouch() {
		return isLeftAnalogTouch;
	}

	public boolean isRightAnalogTouch() {
		return isRightAnalogTouch;
	}

	public boolean isLeftTriggerTouch() {
		return isLeftTriggerTouch;
	}

	public boolean isRightTriggerTouch() {
		return isRightTriggerTouch;
	}

	public float getLeftTriggerAmount() {
		return leftTriggerAmount;
	}

	public float getRightTriggerAmount() {
		return rightTriggerAmount;
	}

	public float getLeftGripAmount() {
		return leftGripAmount;
	}

	public float getRightGripAmount() {
		return rightGripAmount;
	}

	public boolean isLeftValid() {
		return leftValid;
	}

	public boolean isRightValid() {
		return rightValid;
	}

	@Override
	public boolean isTransparent() {
		return false;
	}

	@Override
	public float[] getTransparentObjectPos() {
		return null;
	}

	
}
