package com.theincgi.lwjglApp.mvc.models;

import org.lwjgl.util.vector.Vector3f;

import com.theincgi.lwjglApp.render.Location;
import static com.theincgi.lwjglApp.Utils.inRangeE;

public class AABB {
	float[] p1, p2;
	public AABB(float x1, float y1, float z1, float x2, float y2, float z2) {
		p1 = new float[] {x1, y1, z1};
		p2 = new float[] {x2, y2, z2};
	}
	
	public boolean isIn(Location l) {
		return isIn(l.pos);
	}
	public boolean isIn(Vector3f v) {
		return isIn(v.x, v.y, v.z);
	}
	public boolean isIn(float[] a) {
		return isIn(a[0], a[1], a[2]);
	}
	public boolean isIn(float x, float y, float z) {
		return inRangeE(x, p1[0], p2[0]) &&
			   inRangeE(y, p1[1], p2[1]) &&
			   inRangeE(z, p1[2], p2[2]);
	}
}
