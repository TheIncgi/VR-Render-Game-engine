package com.theincgi.lwjglApp.render.vr;

import org.lwjgl.openvr.TrackedDevicePose;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.theincgi.lwjglApp.render.Drawable;
import com.theincgi.lwjglApp.render.Side;

public interface VRController extends Drawable{

	/**Supported controller types*/
	public enum Type {
		OCULUS_TOUCH,
		VIVE,
		UNKNOWN;
	}
	
	public static class Input{
		Type type;
		Side side;
		boolean isAxis;
		int id;
		public Input(Type type, Side side, boolean isAxis, int id) {
			this.type = type;
			this.side = side;
			this.isAxis = isAxis;
			this.id = id;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + id;
			result = prime * result + (isAxis ? 1231 : 1237);
			result = prime * result + ((side == null) ? 0 : side.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Input other = (Input) obj;
			if (id != other.id)
				return false;
			if (isAxis != other.isAxis)
				return false;
			if (side != other.side)
				return false;
			if (type != other.type)
				return false;
			return true;
		}
		@Override
		public String toString() {
			return "Input [type=" + type + ", side=" + side + ", isAxis=" + isAxis + ", id=" + id + "]";
		}
		
	}

	void updatePoseLeft(int index, TrackedDevicePose tdp);

	void updatePoseRight(int index, TrackedDevicePose tdp);
	
	public Matrix4f getLeftTransform();

	public Matrix4f getRightTransform();
	
	public Vector4f getLeftPointingVector();
	public Vector4f getLeftHoldingVector();
	public Vector4f getRightPointingVector();
	public Vector4f getRightHoldingVector();
	public Vector4f getLeftPointingSource();
	public Vector4f getRightPointingSource();
	public Vector4f getLeftHoldingSource();
	public Vector4f getRightHoldingSource();
}
