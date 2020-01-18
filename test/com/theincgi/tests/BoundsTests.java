package com.theincgi.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.theincgi.lwjglApp.Utils;
import com.theincgi.lwjglApp.misc.RayCast;
import com.theincgi.lwjglApp.mvc.models.OBB;
import com.theincgi.lwjglApp.mvc.models.RadialBounds;

class BoundsTests {

	@Test
	void intersectsTest() {
		RadialBounds b1 = new RadialBounds(0, 0, 0, 1);
		RadialBounds b2 = new RadialBounds(1, 0, 0, 1);
		assertTrue(b1.intersects(b2)); //inclusive
		
	}
	@Test
	void notIntersects() {
		RadialBounds b1 = new RadialBounds(0, 0, 0, 1);
		RadialBounds b2 = new RadialBounds(1.01f, 0, 0, 1);
		assertTrue(b1.intersects(b2)); //inclusive
	}
	@Test
	void raycast() {
		RadialBounds b1 = new RadialBounds(0, 0, 0, 1);
		RayCast r = new RayCast(new Vector4f(-10, 0, 0, 1), new Vector4f(1, 0, 0, 1));
		assertTrue(b1.isRaycastPassthru(r)); //inclusive
	}
	@Test
	void raycast2() {
		RadialBounds b1 = new RadialBounds(0, 0, 0, 1);
		RayCast r = new RayCast(new Vector4f(-10, 0, 0, 1), new Vector4f(1, 1, 0, 1));
		assertFalse(b1.isRaycastPassthru(r)); //inclusive
	}
	@Test
	void projectTest() {
		Vector3f p = new Vector3f(-3, 1, 2);
		Vector3f o = new Vector3f(1, 0, 0);
		Vector3f a = new Vector3f(0, 0, 1);
		Vector3f b = new Vector3f(0, 1, 0);
		
		assertEquals(new Vector3f(1, 1, 2),OBB.projectToPlane(p, o, a, b));
	}
	
	@Test
	void reduceInvertMatrix4f() {
		Matrix4f m = new Matrix4f();
		m.m00 = 0;
		m.m11 = 0;
		m.m22 = 0;
		m.m33 = 0;
		
		m.m02 = -.04f;
		m.m13 = -.0052f;
		Utils.rowReduce(m);
	}
}
