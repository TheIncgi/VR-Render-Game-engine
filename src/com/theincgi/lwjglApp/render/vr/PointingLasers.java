package com.theincgi.lwjglApp.render.vr;

import java.util.Optional;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.theincgi.lwjglApp.misc.MatrixStack;
import com.theincgi.lwjglApp.mvc.models.Bounds;
import com.theincgi.lwjglApp.render.Drawable;
import com.theincgi.lwjglApp.render.Model;
import com.theincgi.lwjglApp.render.ObjManager;
import com.theincgi.lwjglApp.render.Side;

public class PointingLasers implements Drawable{
	private static Optional<Model> laserModel = Optional.empty();
	private VRController controller;
	private float leftLength = 1, rightLength = 1;

	public PointingLasers(VRController controller) {
		if(laserModel.isEmpty())
			laserModel = ObjManager.INSTANCE.get("cmodels/laser/laser.obj","full");
		this.controller = controller;
	}

	@Override
	public void draw() {
		laserModel.ifPresent(model->{
			model.shader.ifPresent(s->{
				try(MatrixStack ms = MatrixStack.modelViewStack.push()){
					Matrix4f.mul(ms.get(), controller.getLeftTransform(), ms.get());
					ms.get().translate(new Vector3f(.0075f, -.025f, .035f));
					ms.get().rotate((float)Math.toRadians(-45), new Vector3f(1, 0 ,0));
					ms.get().scale(new Vector3f(1, 1, leftLength));
					model.drawAtOrigin();
				}
				try(MatrixStack ms = MatrixStack.modelViewStack.push()){
					Matrix4f.mul(ms.get(), controller.getRightTransform(), ms.get());
					ms.get().translate(new Vector3f(-.0075f, -.025f, .035f));
					ms.get().rotate((float)Math.toRadians(-45), new Vector3f(1, 0 ,0));
					ms.get().scale(new Vector3f(1, 1, rightLength));
					model.drawAtOrigin();
				}
			});
		});
	}
	@Override
	public boolean isTransparent() {
		return false;
	}
	@Override
	public float[] getTransparentObjectPos() {
		return null;
	}
	public void setLeftLength(float leftLength) {
		this.leftLength = leftLength;
	}
	public void setRightLength(float rightLength) {
		this.rightLength = rightLength;
	}
}
