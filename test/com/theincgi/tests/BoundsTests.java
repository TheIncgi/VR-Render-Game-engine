package com.theincgi.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.theincgi.lwjglApp.Utils;
import com.theincgi.lwjglApp.misc.RayCast;
import com.theincgi.lwjglApp.mvc.models.OBB;
import com.theincgi.lwjglApp.mvc.models.RadialBounds;

class BoundsTests {

//	@Test //use to be static
//	void intersectsTest() {
//		RadialBounds b1 = new RadialBounds(null, 0, 0, 0, 1);
//		RadialBounds b2 = new RadialBounds(null, 1, 0, 0, 1);
//		assertTrue(b1.intersects(b2)); //inclusive
//		
//	}
//	@Test
//	void notIntersects() {
//		RadialBounds b1 = new RadialBounds(null, 0, 0, 0, 1);
//		RadialBounds b2 = new RadialBounds(null, 1.01f, 0, 0, 1);
//		assertTrue(b1.intersects(b2)); //inclusive
//	}
//	@Test
//	void raycast() {
//		RadialBounds b1 = new RadialBounds(null, 0, 0, 0, 1);
//		RayCast r = new RayCast(new Vector4f(-10, 0, 0, 1), new Vector4f(1, 0, 0, 1));
//		assertTrue(b1.isRaycastPassthru(r)); //inclusive
//	}
//	@Test
//	void raycast2() {
//		RadialBounds b1 = new RadialBounds(null, 0, 0, 0, 1);
//		RayCast r = new RayCast(new Vector4f(-10, 0, 0, 1), (Vector4f) new Vector4f(1, 3, 0, 1).normalize());
//		assertFalse(b1.isRaycastPassthru(r)); //inclusive
//	}
	@Test
	void projectTest1() {
		Vector3f p = new Vector3f(-3, 1, 2);
		Vector3f o = new Vector3f(1, 0, 0);
		Vector3f a = new Vector3f(0, 0, 1);
		Vector3f b = new Vector3f(0, 1, 0);
		
		assertEquals(new Vector3f(1, 1, 2),OBB.projectToPlane(p, o, a, b));
	}
	
	@Test
	void projectTest2() {
		Vector3f p = new Vector3f(.5937f, -.7872f, .155f);
		Vector3f o = new Vector3f(.07029f, -.2216f, -1.395f);
		Vector3f a = new Vector3f(1.135f, .8407f, -.07639f);
		Vector3f b = new Vector3f(-1.135f, -.8497f, .07639f);
		Vector3f.sub(a, o, a);
		Vector3f.sub(b, o, b);	
		
		Vector3f result = OBB.projectToPlane(p, o, a, b); //normal 0.5968486, -0.7876969, 0.15266058
		
		assertEquals(0, result.x, .01f);
		assertEquals(0, result.y, .01f);
		assertEquals(0, result.z, .01f);
	}
	
	@Test
	void toPlaneSpace() {
		Vector3f p = new Vector3f(-1.2752f, -.39743f, 2.8663f);
		Vector3f o = new Vector3f(.07029f, -.2216f, -1.395f);
		Vector3f a = new Vector3f(1.135f, .8407f, -.07639f);
		Vector3f b = new Vector3f(-1.135f, -.8497f, .07639f);
		
		Vector3f.sub(a, o, a);
		Vector3f.sub(b, o, b);
		
		Vector2f result = OBB.pointToPlaneSpace(p, o, a, b);
		
		assertEquals(1, result.x, .1f);
		assertEquals(2, result.y, .1f);
	}
	
	
//	@Test
//	void reduceInvertMatrix4f() {
//		Matrix4f m = new Matrix4f();
//		m.m00 = 0;
//		m.m11 = 0;
//		m.m22 = 0;
//		m.m33 = 0;
//		
//		m.m02 = -.04f;
//		m.m13 = -.0052f;
//		Utils.rowReduce(m);
//	}
}
