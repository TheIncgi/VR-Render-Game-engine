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
import com.theincgi.lwjglApp.mvc.models.OrientedBoundingBox;
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
		Vector4f p = new Vector4f(-3, 1, 2, 1);
		Vector4f o = new Vector4f(1, 0, 0, 1);
		Vector4f a = new Vector4f(0, 0, 1, 0);
		Vector4f b = new Vector4f(0, 1, 0, 0);
		
		assertEquals(new Vector4f(1, 1, 2, 1),OrientedBoundingBox.projectToPlane(p, o, a, b));
	}
	
	@Test
	void projectTest2() {
		Vector4f p = new Vector4f(.5937f, -.7872f, .155f,    1);
		Vector4f o = new Vector4f(.07029f, -.2216f, -1.395f, 1);
		Vector4f a = new Vector4f(1.135f, .8407f, -.07639f,  0);
		Vector4f b = new Vector4f(-1.135f, -.8497f, .07639f, 0);
		Vector4f.sub(a, o, a);
		Vector4f.sub(b, o, b);	
		
		Vector4f result = OrientedBoundingBox.projectToPlane(p, o, a, b); //normal 0.5968486, -0.7876969, 0.15266058
		
		assertEquals(0, result.x, .01f);
		assertEquals(0, result.y, .01f);
		assertEquals(0, result.z, .01f);
	}
	
	@Test
	void toPlaneSpace() {
		Vector4f p = new Vector4f(-1.2752f, -.39743f, 2.8663f,   1);
		Vector4f o = new Vector4f(.07029f, -.2216f, -1.395f,     1);
		Vector4f a = new Vector4f(1.135f, .8407f, -.07639f ,     0);
		Vector4f b = new Vector4f(-1.135f, -.8497f, .07639f,     0 );
		
		Vector4f.sub(a, o, a);
		Vector4f.sub(b, o, b);
		
		Vector2f result = OrientedBoundingBox.pointToPlaneSpace(p, o, a, b);
		
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
