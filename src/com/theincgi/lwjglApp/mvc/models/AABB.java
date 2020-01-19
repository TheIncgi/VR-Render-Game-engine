package com.theincgi.lwjglApp.mvc.models;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.theincgi.lwjglApp.Utils;
import com.theincgi.lwjglApp.misc.Logger;
import com.theincgi.lwjglApp.misc.RayCast;
import com.theincgi.lwjglApp.render.Drawable;
import com.theincgi.lwjglApp.render.Location;
import com.theincgi.lwjglApp.ui.Color;

import static com.theincgi.lwjglApp.Utils.inRangeE;
import static com.theincgi.lwjglApp.Utils.inRangeI;
import static java.lang.Math.min;

import java.util.Optional;

import static java.lang.Math.max;

public class AABB implements Bounds{
	float[] p1, p2;
	Colideable parent;
	public AABB(Colideable parent, float x1, float y1, float z1, float x2, float y2, float z2) {
		this.parent = parent;
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
	
	public boolean isInInclusive(float x, float y, float z) {
		return inRangeI(x, p1[0], p2[0]) &&
			   inRangeI(y, p1[1], p2[1]) &&
			   inRangeI(z, p1[2], p2[2]);
	}
	
	@Override
	public boolean intersects(Bounds other) {
		if(other instanceof AABB) {
			AABB aabb = (AABB)other;
			return AABB.contains(this, aabb) || AABB.contains(aabb, this);
		}else if(other instanceof RadialBounds) {
			return ((RadialBounds)other).intersects(this); //radialBounds has the definition already
		}else if(other instanceof OBB) {
			OBB obb = (OBB) other;
			return obb.intersects(this); //handled in obb
		}else {
			Logger.preferedLogger.w("AABB#intersects", "No definition for the intersection with type "+other.getClass());
			return false;
		}
	}
	
	public boolean isRaycastPassthru(RayCast ray) {
		if(ray.rayDirection.length()==0) return false;
		if(isIn(ray.worldOffset.x, ray.worldOffset.y, ray.worldOffset.z)) {
			ray.setShortResult( ray.worldOffset );
			return true;
		}
		boolean passed = false;
		/**If world offset is in an axis bound*/
		boolean 
			isInX = inRangeI(ray.worldOffset.x, p1[0], p2[0]),
			isInY = inRangeI(ray.worldOffset.y, p1[1], p2[1]),
			isInZ = inRangeI(ray.worldOffset.z, p1[2], p2[2]);
		float 
		  lx = p1[0] - ray.worldOffset.x,
		  ly = p1[1] - ray.worldOffset.y,
		  lz = p1[2] - ray.worldOffset.z,
		  hx = p2[0] - ray.worldOffset.x,
		  hy = p2[1] - ray.worldOffset.y,
		  hz = p2[2] - ray.worldOffset.z;
		
		if(!isInX && ray.rayDirection.x!=0) {
			float scale;
			if(ray.worldOffset.x < p1[0]) //left of the box
				scale = lx / ray.rayDirection.x;
			else
				scale = hx / ray.rayDirection.x;
			
			if(scale <= 0) return false;
			if(inRangeI(ray.worldOffset.y + ray.rayDirection.y * scale, p1[1], p2[1]) &&
				   inRangeI(ray.worldOffset.z + ray.rayDirection.z * scale, p1[2], p2[2])){
				Vector4f out = (Vector4f) new Vector4f(ray.rayDirection).scale(scale);
				Vector4f.add(out, ray.worldOffset, out);
				ray.setShortResult( out );
				passed = true;
			};
		}
		if(!isInY && ray.rayDirection.y!=0) {
			float scale;
			if(ray.worldOffset.y < p1[1])
				scale = ly / ray.rayDirection.y;
			else
				scale = hy / ray.rayDirection.y;
			if(scale <= 0) return false;
			if( inRangeI(ray.worldOffset.x + ray.rayDirection.x * scale, p1[0], p2[0]) &&
				   inRangeI(ray.worldOffset.z + ray.rayDirection.z * scale, p1[2], p2[2])) {
				Vector4f out = (Vector4f) new Vector4f(ray.rayDirection).scale(scale);
				Vector4f.add(out, ray.worldOffset, out);
				ray.setShortResult( out );
				passed = true;
			}
		}
		if(!isInZ && ray.rayDirection.z!=0) {
			float scale;
			if(ray.worldOffset.z < p1[2])
				scale = lz / ray.rayDirection.z;
			else
				scale = hz / ray.rayDirection.z;
			if(scale <= 0) return false;
			if( inRangeI(ray.worldOffset.x + ray.rayDirection.x * scale, p1[0], p2[0]) &&
				   inRangeI(ray.worldOffset.y + ray.rayDirection.y * scale, p1[1], p2[1]) ) {
				Vector4f out = (Vector4f) new Vector4f(ray.rayDirection).scale(scale);
				Vector4f.add(out, ray.worldOffset, out);
				ray.setShortResult( out );
				passed = true;
			}
		}
		if(passed) return true;
		return false; 
		
	};
	
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
	
	@Override
	public void draw() {
		Vector3f origin = new Vector3f(p1[0], p1[1], p1[2]);
		
		Utils.drawVecLine(origin, new Vector3f(p2[0]-p1[0], 0, 0), Color.RED);
		Utils.drawVecLine(origin, new Vector3f(0, p2[1]-p1[1], 0), Color.BLUE);
		Utils.drawVecLine(origin, new Vector3f(0, 0, p2[2]-p1[2]), Color.GREEN);
		
		origin = new Vector3f(p2[0], p2[1], p2[2]);
		Utils.drawVecLine(origin, new Vector3f(-(p2[0]-p1[0]), 0, 0), Color.GRAY);
		Utils.drawVecLine(origin, new Vector3f(0, -(p2[1]-p1[1]), 0), Color.GRAY);
		Utils.drawVecLine(origin, new Vector3f(0, 0, -(p2[2]-p1[2])), Color.GRAY);
		
		origin = new Vector3f(p2[0], p1[1], p1[2]);
		Utils.drawVecLine(origin, new Vector3f(0, p2[1]-p1[1], 0), Color.GRAY);
		Utils.drawVecLine(origin, new Vector3f(0, 0, p2[2]-p1[2]), Color.GRAY);
		
		origin = new Vector3f(p1[0], p2[1], p1[2]);
		Utils.drawVecLine(origin, new Vector3f(p2[0]-p1[0], 0, 0), Color.GRAY);
		Utils.drawVecLine(origin, new Vector3f(0, 0, p2[2]-p1[2]), Color.GRAY);
		
		origin = new Vector3f(p1[0], p1[1], p2[2]);
		Utils.drawVecLine(origin, new Vector3f(p2[0]-p1[0], 0, 0), Color.GRAY);
		Utils.drawVecLine(origin, new Vector3f(0, p2[1]-p1[1], 0), Color.GRAY);
	}
	
	@Override
	public Colideable getParent() {
		return parent;
	}
	
}
