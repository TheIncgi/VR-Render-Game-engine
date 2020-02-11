package com.theincgi.lwjglApp.mvc.models;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.theincgi.lwjglApp.Utils;
import com.theincgi.lwjglApp.misc.Logger;
import com.theincgi.lwjglApp.misc.MatrixStack;
import com.theincgi.lwjglApp.misc.RayCast;
import com.theincgi.lwjglApp.render.Location;
import com.theincgi.lwjglApp.render.Model;
import com.theincgi.lwjglApp.render.ObjManager;
import com.theincgi.lwjglApp.ui.Color;

import static com.theincgi.lwjglApp.Utils.inRangeE;
import static java.lang.Math.abs;

import java.util.Optional;

public class RadialBounds implements Bounds{
	public float radius;
	public final Vector4f center;
	private Colideable parent;
	
	public RadialBounds(float x, float y, float z, float r) {
		center = new Vector4f(x,y,z,1);
		this.radius = r;
	}
	public RadialBounds(Vector4f v, float r) {
		center = v;
		this.radius = r;
	}
	
	@Override
	public boolean isIn(float x, float y, float z) {
		float dx = x-center.x;
		float dy = y-center.y;
		float dz = z-center.z;
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
			if(aabb.isIn(new Vector3f(center))) return true;
			/**Nearest point to the box*/
			float[] near = new float[3];
			boolean inX = inRangeE(center.x, aabb.p1[0], aabb.p2[0]);
			boolean inY = inRangeE(center.y, aabb.p1[1], aabb.p2[1]);
			boolean inZ = inRangeE(center.z, aabb.p1[2], aabb.p2[2]);
			
			near[0] = inX? center.x : closest(center.x, aabb.p1[0], aabb.p2[0]);
			near[1] = inY? center.y : closest(center.y, aabb.p1[1], aabb.p2[1]);
			near[2] = inZ? center.z : closest(center.z, aabb.p1[2], aabb.p2[2]);
			return isIn(near);	
		}else if(other instanceof OrientedBoundingBox) {
			OrientedBoundingBox obb = (OrientedBoundingBox) other;
			return obb.intersects(this); //handled in obb
		}else {
			Logger.preferedLogger.w("RadialBounds#intersects", "No definition for the intersection with "+other.getClass());
			return false;
		}
	}
	
	
	@Override
	public void draw() {
		Optional<Model> model = ObjManager.INSTANCE.get("cmodels/debug/sphereBounds.obj", "full");
		model.ifPresent(mod->{mod.getMaterial().ifPresent(matg->{matg.materials.get("sphereBounds").kd=new Color(0, 0, 0, 0);});});
		try(MatrixStack m = MatrixStack.modelViewStack.push(center)){
			m.get().scale(new Vector3f(radius, radius, radius));
			model.ifPresent(mod->mod.drawAtOrigin());
		}
	}
	
	
	
	@Override
	public boolean isRaycastPassthru(RayCast ray) {
		Vector4f l = new Vector4f(
			center.x - ray.worldOffset.x,
			center.y - ray.worldOffset.y,
			center.z - ray.worldOffset.z,
			0
		);
		float tca = Vector4f.dot(l, ray.rayDirection);
		if(tca < 0) return false;
		Vector4f tcav = (Vector4f) new Vector4f(ray.rayDirection).normalize().scale(tca);
		float d = 
				(float) Math.sqrt(l.length()*l.length() - tca*tca);
//				  (l.x*l.x - tcav.x*tcav.x) 
//				 -(l.y*l.y - tcav.y*tcav.y)
//				 -(l.z*l.z - tcav.z*tcav.z));
		
		if(Float.isNaN(d)) return false;
		if(d > radius) return false;
		
		float th;
		float r2 = radius*radius;
		th = (float) Math.sqrt( radius*radius - d*d );
		
		Vector4f result = Vector4f.sub(tcav, (Vector4f) new Vector4f(tcav).normalize().scale(th), new Vector4f());
		result.w = 1;
		ray.setShortResult( result, this );
		
		return true;
	}
	
	@Override
	public Colideable getParent() {
		return parent;
	}
	@Override
	public void setParent(Colideable parent) {
		this.parent = parent;
	}
	private static float closest(float target, float a, float b) {
		return abs(target-a)>abs(target-b)?b:a;
	}
}
