package com.theincgi.lwjglApp.mvc.models;


import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.theincgi.lwjglApp.Utils;
import com.theincgi.lwjglApp.misc.RayCast;
import com.theincgi.lwjglApp.render.Location;
import com.theincgi.lwjglApp.ui.Color;
import static org.lwjgl.util.vector.Matrix4f.transform;
import static org.lwjgl.util.vector.Vector4f.add;
import static org.lwjgl.util.vector.Vector4f.sub;
import static com.theincgi.lwjglApp.Utils.cross;
import static org.lwjgl.util.vector.Vector4f.dot;

import org.lwjgl.util.vector.Matrix3f;

public class OrientedBoundingBox implements Bounds, Cloneable{
	/**Point of the --- corner, not a 000, rotation should be about the center not corner*/
	Vector4f localizedOrigin;
	Vector4f localizedXDim;
	Vector4f localizedYDim;
	Vector4f localizedZDim;
	Matrix4f transform;
	Colideable parent;
	
	/**w will be set to an acceptable value automaticly*/
	public OrientedBoundingBox(Vector4f localizedOrigin, Vector4f localizedXDim, Vector4f localizedYDim,
			Vector4f localizedZDim) {
		this.localizedOrigin = localizedOrigin;
		this.localizedXDim = localizedXDim;
		this.localizedYDim = localizedYDim;
		this.localizedZDim = localizedZDim;
		localizedOrigin.w = 1;
		localizedXDim.w = 0;
		localizedYDim.w = 0;
		localizedZDim.w = 0;
	}
	public OrientedBoundingBox(Vector4f localizedOrigin, float xDim, float yDim,
			float zDim) {
		this(localizedOrigin, new Vector4f(xDim, 0, 0, 0), new Vector4f(0, yDim, 0, 0), new Vector4f(0,0,zDim,0));
	}
	@Override
	public void setParent(Colideable parent) {
		setParent(parent);
	}
	@Override
	public Colideable getParent() {
		return parent;
	}
	
	public void setTransform(Matrix4f transform) {
		this.transform = transform;
	}
	
	@Override
	public void draw() {
		Vector4f a = new Vector4f(), b = new Vector4f();
		Vector4f tx = new Vector4f(), ty = new Vector4f(), tz = new Vector4f();
		Utils.drawVecLine(Matrix4f.transform(transform, localizedOrigin, a), 	Matrix4f.transform(transform, localizedXDim, tx), Color.RED);
		Utils.drawVecLine(a,													Matrix4f.transform(transform, localizedYDim, ty), Color.GREEN);
		Utils.drawVecLine(a, 													Matrix4f.transform(transform, localizedZDim, tz), Color.BLUE);
		
		Utils.drawVecLine(Matrix4f.transform(transform, Vector4f.add(localizedOrigin, localizedXDim, a), a), ty, Color.GRAY);
		Utils.drawVecLine(Matrix4f.transform(transform, Vector4f.add(localizedOrigin, localizedXDim, a), a), tz, Color.GRAY);
		
		Utils.drawVecLine(Matrix4f.transform(transform, Vector4f.add(localizedOrigin, localizedYDim, a), a), tx, Color.GRAY);
		Utils.drawVecLine(Matrix4f.transform(transform, Vector4f.add(localizedOrigin, localizedYDim, a), a), tz, Color.GRAY);
		
		Utils.drawVecLine(Matrix4f.transform(transform, Vector4f.add(localizedOrigin, localizedZDim, a), a), tx, Color.GRAY);
		Utils.drawVecLine(Matrix4f.transform(transform, Vector4f.add(localizedOrigin, localizedZDim, a), a), ty, Color.GRAY);
		
		Utils.drawVecLine(Matrix4f.transform(transform, Vector4f.add(localizedOrigin, Vector4f.add(localizedXDim, localizedYDim, a), a), a), tz, Color.GRAY);
		Utils.drawVecLine(Matrix4f.transform(transform, Vector4f.add(localizedOrigin, Vector4f.add(localizedXDim, localizedZDim, a), a), a), ty, Color.GRAY);
		Utils.drawVecLine(Matrix4f.transform(transform, Vector4f.add(localizedOrigin, Vector4f.add(localizedYDim, localizedZDim, a), a), a), tx, Color.GRAY);
	}
	
	public boolean isIn(float x, float y, float z) {
		return isIn(new Vector4f(x,y,z,1));
	}
	@Override
	public boolean isIn(float[] a) {
		return isIn(new Vector4f(a[0], a[1], a[2], 1));
	}
	@Override
	public boolean isIn(Location l) {
		return isIn(new Vector4f(l.getX(), l.getY(), l.getZ(), 1));
	}
	@Override
	public boolean isIn(Vector3f v) {
		return isIn(Utils.vec4(v, 1));
	}
	public boolean isIn(Vector4f p) {
		//if point p is projected on to 3 orthogonal sides, and those projections exist within the bounds of those faces
		//then the point is contained within this Oriented Bounding Box
		return false;
	}
	@Override
	public boolean intersects(Bounds other) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean isRaycastPassthru(RayCast ray) {
		// TODO Auto-generated method stub
		//1. Extend ray to faces
		//2. Check if an extended ray is within the bounds of a face
		return false;
	}
	
	//	https://math.stackexchange.com/questions/100439/determine-where-a-vector-will-intersect-a-plane
	//	https://math.stackexchange.com/questions/100761/how-do-i-find-the-projection-of-a-point-onto-a-plane
	//normal abc
	//origin def
	//thePoint xyz
	/**Returns a copy of thePoint moved in the direction parallel to the normal so the returned point exists on the defined plane*/
	public static Vector4f projectToPlane(Vector4f thePoint, Vector4f origin, Vector4f axis1, Vector4f axis2) {
		Vector4f normal = (Vector4f) cross(axis1, axis2, null).normalize();
		float t = 
				( normal.x*origin.x - normal.x*thePoint.x +
						normal.y*origin.y - normal.y*thePoint.y +
						normal.z*origin.z - normal.z*thePoint.z )
				/
				(normal.x*normal.x + normal.y*normal.y + normal.z*normal.z);
		return new Vector4f(thePoint.x + t*normal.x, thePoint.y + t*normal.y, thePoint.z + t*normal.z, 1);
	}
	
	/**Returns scale for <code>axis1</code> and <code>axis2</code>axis1 and axis2 required to reach <code>thePoint</code> when added
	 * <br>Expect a null result if the point isn't even on the plane or axis1 and 2 are parallel */
	public static Vector2f pointToPlaneSpace(Vector4f thePoint, Vector4f origin, Vector4f axis1, Vector4f axis2) {
		Matrix4f a = new Matrix4f();
		a.m00 = axis1.x;   a.m10 = axis2.x;        //0          0
		a.m01 = axis1.y;   a.m11 = axis2.y;        //0          0
		a.m02 = axis1.z;   a.m12 = axis2.z;  a.m22 = 0;  //     0
		/* 0 */               /* 0 */         /* 0 */   a.m33 = 0;
		//a = (Matrix3f) a.invert(); //replace with row reduction inverse thing
		Utils.rowReduce(a);
//		if(a==null) return null;
		Vector4f p2 = sub(thePoint, origin, null);
		Vector4f result = Matrix4f.transform(a, p2, new Vector4f());
		return new Vector2f(result);
	}
	
	public enum Face{
		XP,
		XM,
		YP,
		YM,
		ZP,
		ZM,
	}
	public Vector4f[] getFace(Face face, Vector4f[] outputBuffer){
		Vector4f[] out = outputBuffer==null? new Vector4f[3] : outputBuffer;
		switch (face) {
			case XM:{
				out[0] = Matrix4f.transform(transform, localizedOrigin, new Vector4f());
				out[1] = Matrix4f.transform(transform, localizedZDim, new Vector4f());
				out[2] = Matrix4f.transform(transform, localizedYDim, new Vector4f());
				break;
			}
			case XP:{
				Vector4f tmp = new Vector4f();
				out[0] = Matrix4f.transform(transform, Vector4f.add(localizedOrigin, localizedXDim, tmp), tmp);
				out[1] = Matrix4f.transform(transform, localizedYDim, new Vector4f());
				out[2] = Matrix4f.transform(transform, localizedZDim, new Vector4f());
				break;
			}
			case YM:{
				out[0] = Matrix4f.transform(transform, localizedOrigin, new Vector4f());
				out[1] = Matrix4f.transform(transform, localizedXDim, new Vector4f());
				out[2] = Matrix4f.transform(transform, localizedZDim, new Vector4f());
				break;
			}
			case YP:{
				Vector4f tmp = new Vector4f();
				out[0] = Matrix4f.transform(transform, Vector4f.add(localizedOrigin, localizedYDim, tmp), tmp);
				out[1] = Matrix4f.transform(transform, localizedZDim, new Vector4f());
				out[2] = Matrix4f.transform(transform, localizedXDim, new Vector4f());
				break;
			}
			case ZM:{
				out[0] = Matrix4f.transform(transform, localizedOrigin, new Vector4f());
				out[1] = Matrix4f.transform(transform, localizedYDim, new Vector4f());
				out[2] = Matrix4f.transform(transform, localizedXDim, new Vector4f());
				break;
			}
			case ZP:{
				Vector4f tmp = new Vector4f();
				out[0] = Matrix4f.transform(transform, Vector4f.add(localizedOrigin, localizedZDim, tmp), tmp);
				out[1] = Matrix4f.transform(transform, localizedXDim, new Vector4f());
				out[2] = Matrix4f.transform(transform, localizedYDim, new Vector4f());
				break;
			}
			default:
				return null;
		}
		out[0].w=1; //transformed face origin
		out[1].w=0; //transformed direction 1
		out[2].w=0; //transformed direction 1
		return out;
	}
	
	/**Deep clone*/
	@Override
	public OrientedBoundingBox clone() {
		return new OrientedBoundingBox(new Vector4f(localizedOrigin), new Vector4f(localizedXDim), new Vector4f(localizedYDim), new Vector4f(localizedZDim));
	}
}
