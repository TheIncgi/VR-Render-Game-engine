package com.theincgi.lwjglApp.mvc.models;

import org.lwjgl.util.vector.Vector3f;

import com.theincgi.lwjglApp.Utils;
import com.theincgi.lwjglApp.misc.Logger;
import com.theincgi.lwjglApp.render.Location;
import static com.theincgi.lwjglApp.Utils.inRangeE;
import static java.lang.Math.abs;

public class RadialBounds implements Bounds{
	float radius;
	float[] center;
	public RadialBounds(float x, float y, float z, float r) {
		center = new float[] {x,y,z};
		this.radius = r;
	}
	
	@Override
	public boolean isIn(float x, float y, float z) {
		float dx = x-center[0];
		float dy = y-center[1];
		float dz = z-center[2];
		return Math.sqrt(dx*dx + dy*dy + dz*dz) <= radius;
	}
	@Override
	public boolean isIn(float[] a) {
		return isIn(a[0], a[1], a[2]);
	}
	@Override
	public boolean isIn(Location l) {
		return isIn(l.getX(), l.getY(), l.getZ());
	}

	@Override
	public boolean isIn(Vector3f v) {
		return isIn(v.x, v.y, v.z);
	}

	@Override
	public boolean intersects(Bounds other) {
		if(other instanceof RadialBounds) {
			RadialBounds rother = (RadialBounds)other;
			return Utils.distVec3(center, rother.center) <= this.radius+rother.radius;
		}else if(other instanceof AABB) {
			AABB aabb = (AABB)other;
			if(aabb.isIn(center)) return true;
			/**Nearest point to the box*/
			float[] near = new float[3];
			boolean inX = inRangeE(center[0], aabb.p1[0], aabb.p2[0]);
			boolean inY = inRangeE(center[1], aabb.p1[1], aabb.p2[1]);
			boolean inZ = inRangeE(center[2], aabb.p1[2], aabb.p2[2]);
			
			near[0] = inX? center[0] : closest(center[0], aabb.p1[0], aabb.p2[0]);
			near[1] = inY? center[1] : closest(center[1], aabb.p1[1], aabb.p2[1]);
			near[2] = inZ? center[2] : closest(center[2], aabb.p1[2], aabb.p2[2]);
			return isIn(near);	
		}else {
			Logger.preferedLogger.w("RadialBounds#intersects", "No definition for the intersection with "+other.getClass());
			return false;
		}
	}
	
	private static float closest(float target, float a, float b) {
		return abs(target-a)>abs(target-b)?b:a;
	}
}
