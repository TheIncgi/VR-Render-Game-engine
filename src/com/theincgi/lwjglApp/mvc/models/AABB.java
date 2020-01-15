package com.theincgi.lwjglApp.mvc.models;

import org.lwjgl.util.vector.Vector3f;

import com.theincgi.lwjglApp.misc.Logger;
import com.theincgi.lwjglApp.render.Location;
import static com.theincgi.lwjglApp.Utils.inRangeE;
import static java.lang.Math.min;
import static java.lang.Math.max;

public class AABB implements Bounds{
	float[] p1, p2;
	public AABB(float x1, float y1, float z1, float x2, float y2, float z2) {
		p1 = new float[] {min(x1,x2), min(y1,y2), min(z1,z2)};
		p2 = new float[] {max(x1,x2), max(y1,y2), max(z1,z2)};
	}
	
	@Override
	public boolean isIn(Location l) {
		return isIn(l.pos);
	}
	@Override
	public boolean isIn(Vector3f v) {
		return isIn(v.x, v.y, v.z);
	}
	@Override
	public boolean isIn(float[] a) {
		return isIn(a[0], a[1], a[2]);
	}
	@Override
	public boolean isIn(float x, float y, float z) {
		return inRangeE(x, p1[0], p2[0]) &&
			   inRangeE(y, p1[1], p2[1]) &&
			   inRangeE(z, p1[2], p2[2]);
	}
	
	@Override
	public boolean intersects(Bounds other) {
		if(other instanceof AABB) {
			AABB aabb = (AABB)other;
			return AABB.contains(this, aabb) || AABB.contains(aabb, this);
		}else if(other instanceof RadialBounds) {
			return ((RadialBounds)other).intersects(this); //radialBounds has the definition already
		}else {
			Logger.preferedLogger.w("AABB#intersects", "No definition for the intersection with type "+other.getClass());
			return false;
		}
	}
	/**Checks if a point in b is contained by a*/
	private static boolean contains(AABB a, AABB b) {
		return //any point from b exists in a?
				a.isIn(b.p1[0], b.p1[1], b.p1[2]) || // ---
				a.isIn(b.p1[0], b.p1[1], b.p2[2]) || // --+
				a.isIn(b.p1[0], b.p2[1], b.p1[2]) || // -+-
				a.isIn(b.p1[0], b.p2[1], b.p2[2]) || // -++
				a.isIn(b.p2[0], b.p1[1], b.p1[2]) || // +--
				a.isIn(b.p2[0], b.p1[1], b.p2[2]) || // +-+
				a.isIn(b.p2[0], b.p2[1], b.p1[2]) || // ++-
				a.isIn(b.p2[0], b.p2[1], b.p2[2]);   // +++
	}
}
