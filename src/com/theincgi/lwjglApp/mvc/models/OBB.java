package com.theincgi.lwjglApp.mvc.models;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.theincgi.lwjglApp.Utils;
import com.theincgi.lwjglApp.misc.Logger;
import com.theincgi.lwjglApp.misc.RayCast;
import com.theincgi.lwjglApp.render.Drawable;
import com.theincgi.lwjglApp.render.Location;
import com.theincgi.lwjglApp.ui.Color;

import static com.theincgi.lwjglApp.Utils.vec4;
import static org.lwjgl.util.vector.Vector3f.add;

import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;

import static org.lwjgl.util.vector.Vector3f.sub;

import java.util.Optional;

import static org.lwjgl.util.vector.Vector3f.dot;
import static org.lwjgl.util.vector.Vector3f.cross;
import static com.theincgi.lwjglApp.Utils.inRangeI;


/**This class could be extended further to use user defined corners for all 8 points to create<br>
 * a Oriented Bounding Rectangular Parallelepiped (Cuboid) with minimal changes at the cost of additional memory usage*/
public class OBB implements Bounds, Cloneable{

	/**Renaming needed, origin (---), right(+00), forward(00+) up(0+0)*/
	Vector3f
	origin,
	right,
	forward,
	up;
	private Colideable parent;

	/**Origin, the negative coord on all 3 axis, x, y, z, relative direction of each axis*/
	public OBB(Vector3f origin, Vector3f x, Vector3f y, Vector3f z) {
		this.origin = origin;
		this.right = x;
		this.up = y;
		this.forward = z;
	}

	/**Origin, the negative coord on all 3 axis, and then the x width, y height, and z length*/
	public OBB(Vector3f origin, float width, float height, float length) {
		this.origin = origin;
		this.right = new Vector3f(width, 0, 0);
		this.up = new Vector3f(0, height, 0);
		this.forward = new Vector3f(0, 0, length);
	}


	public void transform(Matrix4f transform) {
		Vector4f tmp = vec4(origin, 1); //only origin translates
		Matrix4f.transform(transform, tmp, tmp);
		origin = new Vector3f(tmp);

		tmp = vec4(right, 0);
		Matrix4f.transform(transform, tmp, tmp);
		right = new Vector3f(tmp);

		tmp = vec4(up, 0);
		Matrix4f.transform(transform, tmp, tmp);
		up = new Vector3f(tmp);

		tmp = vec4(forward, 0);
		Matrix4f.transform(transform, tmp, tmp);
		forward = new Vector3f(tmp);
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
		return isIn(new Vector3f(x, y, z));
	}
	@Override
	public boolean isIn(float[] a) {
		return isIn(a[0], a[1], a[2]);
	}
	@Override
	public boolean isIn(Vector3f v) {
		return 
				isPointInfrontOfFace(v, origin, right, forward) &&
				isPointInfrontOfFace(v, origin, up,    forward) &&
				isPointInfrontOfFace(v, origin, up,    right  );
	}
	@Override
	public boolean isIn(Location l) {
		return isIn(l.getX(), l.getY(), l.getZ());
	}

	@Override
	public boolean intersects(Bounds other) {
		if(other instanceof OBB) {
			OBB otherObb = (OBB) other;
			return rayOfThisIntersectsOther(other) || otherObb.rayOfThisIntersectsOther(this) || //edge intersects face
					pointOfThisIsInOther(otherObb) || otherObb.pointOfThisIsInOther(this);       //or point inside
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
			Vector3f center = new Vector3f(rb.center);
			Vector3f temp = new Vector3f();
			if(isIn(center)) return true;
			if(sub(projectToPlane(center, origin, right, forward), center, temp).length() <= rb.radius) return true;
			if(sub(projectToPlane(center, origin, up,    forward), center, temp).length() <= rb.radius) return true;
			if(sub(projectToPlane(center, origin, up,      right), center, temp).length() <= rb.radius) return true;

			Vector3f o = new Vector3f(origin);
			add(o, add(right, add(forward, up, temp = new Vector3f()), temp), o);
			Vector3f left = (Vector3f) new Vector3f(right).scale(-1);
			Vector3f down = (Vector3f) new Vector3f(up).scale(-1);
			Vector3f back = (Vector3f) new Vector3f(forward).scale(-1);

			if(sub(projectToPlane(center, o, back, left), center, temp).length() <= rb.radius) return true;
			if(sub(projectToPlane(center, o, back, down), center, temp).length() <= rb.radius) return true;
			if(sub(projectToPlane(center, o, left, down), center, temp).length() <= rb.radius) return true;

			return false;
			//center contained, or
			//projection onto face in face bounds & normal length to center is <= radius
			//must check all faces
		}else {
			Logger.preferedLogger.w("OBB#intersects", "OBB intersection with class "+other.getClass().getName()+" is not defined");
		}
		return false;
	}

	private boolean pointOfThisIsInOther(Bounds other) {
		Vector3f tmp = new Vector3f(origin);
		if(other.isIn(tmp)) return true;
		add(tmp, right, tmp);
		if(other.isIn(tmp)) return true;
		add(origin, up, tmp);
		if(other.isIn(tmp)) return true;
		add(origin, forward, tmp);
		if(other.isIn(tmp)) return true;

		add(tmp, up, tmp);  //fow up
		if(other.isIn(tmp)) return true;
		add(add(origin, right, tmp), up, tmp); //right up
		if(other.isIn(tmp)) return true;
		add(add(origin, forward, tmp), right, tmp); //fow right
		if(other.isIn(tmp)) return true;
		add(tmp, up, tmp); //fow right up
		if(other.isIn(tmp)) return true;

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
		Vector3f o, n;
		o = origin;
		boolean passed = false;

		n = (Vector3f) cross(up, right, new Vector3f()).normalize(); //local -z
		if(intersectRayWithPlane(ray, o, n, up, right)) passed = true;
		n = (Vector3f) cross(forward, up, new Vector3f()).normalize(); //local -x
		if(intersectRayWithPlane(ray, o, n, forward, up)) passed = true;
		n = (Vector3f) cross(right, forward, new Vector3f()).normalize(); //local -y
		if(intersectRayWithPlane(ray, o, n, right, forward)) passed = true;

		Vector3f tmp;
		add(o, add(right, add(forward, up, tmp = new Vector3f()), tmp), o);
		Vector3f left = (Vector3f) new Vector3f(right).scale(-1);
		Vector3f down = (Vector3f) new Vector3f(up).scale(-1);
		Vector3f back = (Vector3f) new Vector3f(forward).scale(-1);
		n = (Vector3f) cross(left, down, new Vector3f()).normalize(); //local +z
		if(intersectRayWithPlane(ray, o, n, left, down)) passed = true;
		n = (Vector3f) cross(down, back, new Vector3f()).normalize(); //local +x
		if(intersectRayWithPlane(ray, o, n, down, back)) passed = true;
		n = (Vector3f) cross(back, left, new Vector3f()).normalize(); //local +y
		if(intersectRayWithPlane(ray, o, n, back, left)) passed = true;
		return passed;
	}

	//	public static boolean isRayThruFace(RayCast ray, Vector3f origin, Vector3f normal) {
	//		Vector3f dir = (Vector3f) new Vector3f(ray.rayDirection).normalize();
	//		Vector3f rsrc = new Vector3f(ray.worldOffset);
	//		if(dot(normal, dir) <= 0) return false; 
	//		float t = ( dot(normal, origin) - dot(normal, rsrc)) / dot(normal, dir);
	//		Vector3f result = add(((Vector3f)dir.scale(t)), rsrc, new Vector3f());
	//		ray.setShortResult(Utils.vec4(result, 0));
	//		return true;
	//	}
	//	
	//	

	//https://stackoverflow.com/a/58819973
	public boolean intersectRayWithPlane(RayCast ray, Vector3f planeOrigin, Vector3f planeNormal, Vector3f dim1, Vector3f dim2) { 
		Vector3f v = new Vector3f(ray.rayDirection);
		Vector3f p = new Vector3f(ray.worldOffset);
		float d = Vector3f.dot(planeOrigin, planeNormal); //ax+by+cz=d abc is normal, xyz was the origin
		float denom = Vector3f.dot(planeNormal, v);

		// Prevent divide by zero:
		if (Math.abs(denom) <= .0001)
			return false;

		// If you want to ensure the ray reflects off only
		// the "top" half of the plane, use this instead:
		if (-denom <= .0001)
			return false;

		float t = -(dot(planeNormal, p) + d) / dot(planeNormal, v);

		if (t <= 0)
			return false;

		ray.setShortResult(
				Utils.vec4(Vector3f.add(p, (Vector3f) v.scale(t), p), 1),
				this
				);
		Vector2f local = pointToPlaneSpace(new Vector3f(ray.result.get()), planeOrigin, dim1, dim2);
		if(!inRangeI(local.x, 0, 1)) return false;
		if(!inRangeI(local.y, 0, 1)) return false;
		return true;
	}
	//	https://math.stackexchange.com/questions/100439/determine-where-a-vector-will-intersect-a-plane
	//	https://math.stackexchange.com/questions/100761/how-do-i-find-the-projection-of-a-point-onto-a-plane
	//normal abc
	//origin def
	//thePoint xyz
	/**Returns a copy of thePoint moved in the direction parallel to the normal so the returned point exists on the defined plane*/
	public static Vector3f projectToPlane(Vector3f thePoint, Vector3f origin, Vector3f axis1, Vector3f axis2) {
		Vector3f normal = (Vector3f) cross(axis1, axis2, new Vector3f()).normalize();
		float t = 
				( normal.x*origin.x - normal.x*thePoint.x +
						normal.y*origin.y - normal.y*thePoint.y +
						normal.z*origin.z - normal.z*thePoint.z )
				/
				(normal.x*normal.x + normal.y*normal.y + normal.z*normal.z);
		return new Vector3f(thePoint.x + t*normal.x, thePoint.y + t*normal.y, thePoint.z + t*normal.z);
	}

	/**Returns scale for <code>axis1</code> and <code>axis2</code>axis1 and axis2 required to reach <code>thePoint</code> when added
	 * <br>Expect a null result if the point isn't even on the plane or axis1 and 2 are parallel */
	public static Vector2f pointToPlaneSpace(Vector3f thePoint, Vector3f origin, Vector3f axis1, Vector3f axis2) {
		Matrix3f a = new Matrix3f();
		a.m00 = axis1.x;   a.m10 = axis2.x;        //0 
		a.m01 = axis1.y;   a.m11 = axis2.y;        //0
		a.m02 = axis1.z;   a.m12 = axis2.z;  a.m22 = 0; 
		//a = (Matrix3f) a.invert(); //replace with row reduction inverse thing
		Utils.rowReduce(a);
//		if(a==null) return null;
		Vector3f result = Matrix3f.transform(a, thePoint, new Vector3f());
		Vector3f ori2d  = Matrix3f.transform(a, origin, new Vector3f());//TODO transform point before transforming by matrix
		sub(result, ori2d, result);
		return new Vector2f(result);
	}
	/**
	 * Check if some point that has been projected onto a plane defined by two vectors<br>
	 * has a point with its area<br>
	 * really just check if the scale for the x y values is 0 to 1 inclusive, meaning that it is inside the face<br>
	 * Returns false if arg is null
	 * */
	public static boolean isPointProjectedIntoParallelagram(Vector2f localCoords) {
		if(localCoords==null) return false;
		return inRangeI(localCoords.x, 0, 1) &&inRangeI(localCoords.y, 0, 1);
	}
	public static boolean isPointInfrontOfFace(Vector3f testPoint, Vector3f origin, Vector3f axis1, Vector3f axis2) {
		return isPointProjectedIntoParallelagram( pointToPlaneSpace(projectToPlane(testPoint, origin, axis1, axis2), origin, axis1, axis2) );
	}

	@Override
	public OBB clone() {
		return new OBB(new Vector3f(origin), new Vector3f(right), new Vector3f(up), new Vector3f(forward));
	}

	@Override
	public Colideable getParent() {
		return parent;
	}
	@Override
	public void setParent(Colideable parent) {
		this.parent = parent;
	}

	@Override
	public void draw() {
		Utils.drawVecLine(origin, right, 	Color.RED);
		Utils.drawVecLine(origin, up , 		Color.GREEN);
		Utils.drawVecLine(origin, forward, 	Color.BLUE);

		Vector3f o = new Vector3f(origin), temp = new Vector3f();
		add(o, add(right, add(forward, up, temp = new Vector3f()), temp), o);
		Vector3f left = (Vector3f) new Vector3f(right).scale(-1);
		Vector3f down = (Vector3f) new Vector3f(up).scale(-1);
		Vector3f back = (Vector3f) new Vector3f(forward).scale(-1);
		Utils.drawVecLine(o, left, Color.GRAY);
		Utils.drawVecLine(o, down, Color.GRAY);
		Utils.drawVecLine(o, back, Color.GRAY);

	}

}
