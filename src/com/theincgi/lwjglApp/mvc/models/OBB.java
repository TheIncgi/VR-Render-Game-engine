package com.theincgi.lwjglApp.mvc.models;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.theincgi.lwjglApp.misc.Logger;
import com.theincgi.lwjglApp.misc.RayCast;
import com.theincgi.lwjglApp.render.Location;
import static com.theincgi.lwjglApp.Utils.vec4;
import static org.lwjgl.util.vector.Vector3f.add;

public class OBB implements Bounds{
	Vector3f
		origin,
		right,
		forward,
		up;

	public OBB(Vector3f origin, Vector3f right, Vector3f forward, Vector3f up) {
		super();
		this.origin = origin;
		this.right = right;
		this.forward = forward;
		this.up = up;
	}
	
	//expected rules of collision:
	//1. An OBB will have at least 1 line pass through 1 face of the other OBB (may have to flip which ones faces vs which ones lines are being tested)
	//2. An OBB can still be colliding if it is entirely contained by another OBB, in which case a corner must be inside of the other OBB.
	
	//expected rules of containment:
	//1. a point projected on to 3 different (non parallel) faces should exist within that face (not be on the plane extended off of that face)
	//2. a point should have a negative normal to get to it, this requires 6 tests however...
	//   
	@Override
	public boolean isIn(float x, float y, float z) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean isIn(float[] a) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean isIn(Vector3f v) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean isIn(Location l) {
		return isIn(l.getX(), l.getY(), l.getZ());
	}
	
	private boolean pointProjectsOntoFace(Vector3f point, Vector3f s1, Vector3f s2) {
		//TODO
		return false;
	}
	@Override
	public boolean intersects(Bounds other) {
		if(other instanceof OBB) {
			OBB otherObb = (OBB) other;
			return rayOfThisIntersectsOther(other) || otherObb.rayOfThisIntersectsOther(this);
		}else if(other instanceof AABB) {
			AABB aabb = (AABB)other;
			OBB temp = new OBB(
					new Vector3f(aabb.p1[0], aabb.p1[1], aabb.p1[2]),
					new Vector3f(aabb.p2[0]-aabb.p1[0], 0, 0),
					new Vector3f(0, aabb.p2[1]-aabb.p1[1], 0),
					new Vector3f(0, 0, aabb.p2[2]-aabb.p1[2])
			);
			return intersects(temp);
		}else if(other instanceof RadialBounds) {
			RadialBounds rb = (RadialBounds) other;
			//center contained, or
			//projection onto face in face bounds & normal length to center is <= radius
			//must check all faces
			Logger.preferedLogger.w("OBB#intersects", "OBB intersection with RadialBounds not setup yet!");
		}else {
			Logger.preferedLogger.w("OBB#intersects", "OBB intersection with class "+other.getClass().getName()+" is not defined");
		}
		return false;
	}

	private boolean rayOfThisIntersectsOther(Bounds other) {
		Vector3f tmp3 = new Vector3f();
		RayCast test = new RayCast(new Vector4f(), new Vector4f());
		
		//right
		test.rayDirection = vec4(right, 0);
		test.worldOffset  = vec4(origin,0);
		if(other.isRaycastPassthru(test)) return true;
		
		test.worldOffset  = vec4(add(origin, up, tmp3), 0);
		if(other.isRaycastPassthru(test)) return true;
		
		test.worldOffset  = vec4(add(origin, forward, tmp3), 0);
		if(other.isRaycastPassthru(test)) return true;
		
		test.worldOffset  = vec4(add(tmp3, up, tmp3), 0);
		if(other.isRaycastPassthru(test)) return true;
		
		//up
		test.rayDirection = vec4(up, 0);
		test.worldOffset  = vec4(origin,0);
		if(other.isRaycastPassthru(test)) return true;
		
		test.worldOffset  = vec4(add(origin, forward, tmp3), 0);
		if(other.isRaycastPassthru(test)) return true;
		
		test.worldOffset  = vec4(add(origin, right, tmp3), 0);
		if(other.isRaycastPassthru(test)) return true;
		
		test.worldOffset  = vec4(add(tmp3, forward, tmp3), 0);
		if(other.isRaycastPassthru(test)) return true;
		
		//forward
		test.rayDirection = vec4(forward, 0);
		test.worldOffset  = vec4(origin,0);
		if(other.isRaycastPassthru(test)) return true;
		
		test.worldOffset  = vec4(add(origin, up, tmp3), 0);
		if(other.isRaycastPassthru(test)) return true;
		
		test.worldOffset  = vec4(add(origin, right, tmp3), 0);
		if(other.isRaycastPassthru(test)) return true;
		
		test.worldOffset  = vec4(add(tmp3, up, tmp3), 0);
		if(other.isRaycastPassthru(test)) return true;
		
		return false;
	}
	@Override
	public boolean isRaycastPassthru(RayCast ray) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
