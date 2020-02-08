package com.theincgi.lwjglApp;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Optional;

import org.lwjgl.opengl.GL11;
import org.lwjgl.openvr.HmdMatrix34;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.theincgi.lwjglApp.misc.MatrixStack;
import com.theincgi.lwjglApp.render.Location;
import com.theincgi.lwjglApp.render.Material;
import com.theincgi.lwjglApp.render.MaterialGroup;
import com.theincgi.lwjglApp.render.Model;
import com.theincgi.lwjglApp.render.ObjManager;
import com.theincgi.lwjglApp.render.vr.VRUtil;
import com.theincgi.lwjglApp.ui.Color;

public class Utils {
	public static final Vector3f 
	AXIS_UP = new Vector3f(0, 1, 0),
	AXIS_RIGHT = new Vector3f(1, 0, 0),
	AXIS_OUT = new Vector3f(0, 0, 1);


	/**Exclusive In range test*/
	public static boolean inRangeE(float x, float min, float max) {
		return min<=x && x < max;
	}
	/**Exclusive In range test*/
	public static boolean inRangeE(int x, int min, int max) {
		return min<=x && x < max;
	}
	/**Exclusive In range test*/
	public static boolean inRangeE(long x, long min, long max) {
		return min<=x && x < max;
	}

	/**Inclusive In range test*/
	public static boolean inRangeI(float x, float min, float max) {
		return min<=x && x <= max;
	}
	/**Inclusive In range test*/
	public static boolean inRangeI(int x, int min, int max) {
		return min<=x && x <= max;
	}
	/**Inclusive In range test*/
	public static boolean inRangeI(long x, long min, long max) {
		return min<=x && x <= max;
	}

	public static double max(double a, double... args) {
		double m = a;
		for (int i = 0; i < args.length; i++) {
			m = Math.max(m, args[i]);
		}
		return m;
	}
	public static double min(double a, double... args) {
		double m = a;
		for (int i = 0; i < args.length; i++) {
			m = Math.max(m, args[i]);
		}
		return m;
	}
	public static float max(float a, float... args) {
		float m = a;
		for (int i = 0; i < args.length; i++) {
			m = Math.max(m, args[i]);
		}
		return m;
	}
	public static float min(float a, float... args) {
		float m = a;
		for (int i = 0; i < args.length; i++) {
			m = Math.max(m, args[i]);
		}
		return m;
	}

	public static FloatBuffer toBuffer(float... floats) {
		FloatBuffer buf = MemoryUtil.memAllocFloat(floats.length);
		buf.put(floats).flip();
		return buf;
	}
	public static IntBuffer toBuffer(int... ints) {
		IntBuffer buf = MemoryUtil.memAllocInt(ints.length);
		buf.put(ints).flip();
		return buf;
	}
	public static void freeBuffer(Buffer buf) {
		if(buf!=null)
			MemoryUtil.memFree(buf);
	}

	public static Matrix4f fromT(HmdMatrix34 h) {
		return fromT(h, new Matrix4f());
	}
	/**adds an extra row with identity and transposes*/
	public static Matrix4f fromT(HmdMatrix34 h, Matrix4f dest) {
		Matrix4f out = dest ==null? new Matrix4f() : dest;
		out.m00 = h.m(0);  out.m10 = h.m(1);   out.m20 = h.m(2); out.m30 = h.m(3);
		out.m01 = h.m(4);  out.m11 = h.m(5);   out.m21 = h.m(6); out.m31 = h.m(7);
		out.m02 = h.m(8);  out.m12 = h.m(9);   out.m22 = h.m(10);out.m32 = h.m(11);
		/*   03 = 0             13 =0               23 = 0    */ out.m33 = 1;                                     
		return out;
	}


	/**adds an extra row with identity and transposes*/
	public static Matrix4f from(HmdMatrix34 h) { 
		Matrix4f out = new Matrix4f();
		out.m00 = h.m(0);  out.m10 = h.m(4);  out.m20 = h.m(8);  //  m30 = 0
		out.m01 = h.m(1);  out.m11 = h.m(5);  out.m21 = h.m(9);  //  m31 = 0 
		out.m02 = h.m(2);  out.m12 = h.m(6);  out.m22 = h.m(10); //  m32 = 0
		out.m03 = h.m(3);  out.m13 = h.m(7);  out.m23 = h.m(11); out.m33 = 1;
		return out;
	}
	/**
	 * Takes 2 length 3 float arrays and provides the distance<br>
	 * longer arrays will only use the first 3 elements
	 * */
	public static float distVec3(float[] a, float[] b) {
		float dx = b[0] - a[0],
				dy = b[1] - a[1],
				dz = b[2] - a[2];
		return (float) Math.sqrt( dx*dx + dy*dy + dz*dz );
	}
	/**
	 * Takes the xyz values and provides the distance<br>
	 * */
	public static float distVec3(Vector4f a, Vector4f b) {
		float dx = b.x - a.x,
				dy = b.y - a.y,
				dz = b.z - a.z;
		return (float) Math.sqrt( dx*dx + dy*dy + dz*dz );
	}

	/**Returns a normaly distributed float between -range and range, non inclusive*/
	public static float ndRandom(float range) {
		return (float)( Math.random()+Math.random()+Math.random() - Math.random() - Math.random() - Math.random() )/6*range;
	}
	public static Vector4f vec4(Vector3f v, float w) {
		return new Vector4f(v.x, v.y, v.z, w);
	}


	private static Optional<Model> m = Optional.empty();
	
	public static void drawVecLine(Vector4f origin, Vector4f vector, final Color color) {
		drawVecLine(new Vector3f(origin), new Vector3f(vector), color);
	}
	public static void drawVecLine(Vector3f origin, Vector3f vector, final Color color) {
		if(m.isEmpty())
			m = ObjManager.INSTANCE.get("cmodels/laser/vector.obj", "flat");
		m.ifPresent(mod->{
			float l;
			mod.shader.ifPresent(s->{
				s.bind();
				s.trySetUniform("color", color);
			});
			Location location = new Location();
			float yaw = (float) -Math.atan2(vector.z, vector.x);
			float pitch = (float) Math.asin(vector.y / (l = vector.length()));
			if(Float.isNaN(pitch)) return;
			Matrix4f matrix = new Matrix4f();
			matrix.translate(origin);

			matrix.rotate(yaw, AXIS_UP);
			matrix.rotate(pitch, AXIS_OUT);
			matrix.rotate((float) Math.toRadians(-90), AXIS_UP);

			matrix.scale(new Vector3f(l, l, l));

			try(MatrixStack m1 = MatrixStack.modelViewStack.push(matrix)){
				mod.drawAtOrigin();
			}
			//			location.setX(origin.x);
			//			location.setY(origin.y);
			//			location.setZ(origin.z);
			//			location.setYaw((float) Math.toDegrees(yaw)-90);
			//			location.setPitch((float) Math.toDegrees(pitch));
			//			mod.drawAt(location);
		});
	}

	/**
	 * The cross product of two vectors.<br/>
	 * Similar except that this is for vec4 and will leave w as 0 (since these should be two directions and directions shouldn't be translated)
	 * @param left The LHS vector
	 * @param right The RHS vector
	 * @param dest The destination result, or null if a new vector is to be created
	 * @return left cross right
	 */
	public static Vector4f cross(
			Vector4f left,
			Vector4f right,
			Vector4f dest)
	{

		if (dest == null)
			dest = new Vector4f();

		dest.set(
				left.y * right.z - left.z * right.y,
				right.x * left.z - right.z * left.x,
				left.x * right.y - left.y * right.x
				);
		dest.w = 0;
		return dest;
	}
	/**Row reduces to invert the matrix as best it can, some rows may have only zeros at the end
	 */
	public static void rowReduce(Matrix4f matrix) {
		Matrix4f inverse = new Matrix4f();//identity
			//make x,y 1
			//make x,y+n 0
			for(int x = 0; x<4; x++) { //col
				for(int y = 0; y<4; y++) { //row
					int row2 = -1;
					float value = 0;
					for(int i = 1; i<4; i++) {
						if((value = get(matrix, (y+i)%4, x)) != 0) {//prefers rows below current
							row2 = (y+i)%4;
							break;
						}
					}

					if(row2==-1) continue; //all others are 0

					float factor;
					if(x==y) { //make 1
						float difference = get(matrix, y, x) - 1;
						factor = -difference / value; //if xy==1 at 7 | and value = 2 | dif=6, factor becomes -3 | 7 + -3*2 = 1
					}else { //make 0
						float difference = get(matrix, y, x);
						factor = -difference / value; //if xy==1 at 7 | and value = 2 | dif=7, factor becomes -3.5 | 7 + -3.5*2 = 0
					}
					if(factor==0) continue; //no point
					rowCombine(matrix, inverse, row2, y, factor);
				}
			}
		
		matrix.load(inverse);
	}
	/**Row reduces to invert the matrix as best it can, some rows may have only zeros at the end
	 */
	public static void rowReduce(Matrix3f matrix) {
		Matrix3f inverse = new Matrix3f();//identity
		//make x,y 1
		//make x,y+n 0
		for(int x = 0; x<3; x++) { //col
			for(int y = 0; y<3; y++) { //row
				int row2 = -1;
				float value = 0;
				for(int i = 1; i<3; i++) {
					if((value = get(matrix, (y+i)%3, x)) != 0) {//prefers rows below current
						row2 = (y+i)%3;
						break;
					}
				}
				
				if(row2==-1) continue; //all others are 0
				
				float factor;
				if(x==y) { //make 1
					float difference = get(matrix, y, x) - 1;
					factor = -difference / value; //if xy==1 at 7 | and value = 2 | dif=6, factor becomes -3 | 7 + -3*2 = 1
				}else { //make 0
					float difference = get(matrix, y, x);
					factor = -difference / value; //if xy==1 at 7 | and value = 2 | dif=7, factor becomes -3.5 | 7 + -3.5*2 = 0
				}
				if(factor==0) continue; //no point
				rowCombine(matrix, inverse, row2, y, factor);
			}
		}
		
		matrix.load(inverse);
	}
	private static float get(Matrix4f m, int row, int col) {
		int i = row+col*4;
		switch(i) {
		case 0:  return m.m00;
		case 1:  return m.m01;
		case 2:  return m.m02;
		case 3:  return m.m03;
		case 4:  return m.m10;
		case 5:  return m.m11;
		case 6:  return m.m12;
		case 7:  return m.m13;
		case 8:  return m.m20;
		case 9:  return m.m21;
		case 10: return m.m22;
		case 11: return m.m23;
		case 12: return m.m30;
		case 13: return m.m31;
		case 14: return m.m32;
		case 15: return m.m33;
		}
		throw new IndexOutOfBoundsException(i);
	}
	private static float get(Matrix3f m, int row, int col) {
		int i = row+col*3;
		switch(i) {
		case 0:  return m.m00;
		case 1:  return m.m01;
		case 2:  return m.m02;
		case 3:  return m.m10;
		case 4:  return m.m11;
		case 5:  return m.m12;
		case 6:  return m.m20;
		case 7:  return m.m21;
		case 8: return m.m22;
		}
		throw new IndexOutOfBoundsException(i);
	}
	private static float getM3(FloatBuffer matrixBuffer, int row, int col) {
		return matrixBuffer.get( row + 4 * col ); //ordered by [ col[ r,r,r,r]...
	}
	private static void rowCombine(Matrix4f left, Matrix4f right, int rowsrc, int rowtarget, float factor) {
		rowCombine(left, rowsrc, rowtarget, factor);
		rowCombine(right, rowsrc, rowtarget, factor);
	}
	private static void rowCombine(Matrix4f left, int rowsrc, int rowtarget, float factor) {
		float a=0,b=0,c=0,d=0;
		switch (rowsrc) {
		case 0:
			a = left.m00;
			b = left.m10;
			c = left.m20;
			d = left.m30;
			break;
		case 1:
			a = left.m01;
			b = left.m11;
			c = left.m21;
			d = left.m31;
			break;
		case 2:
			a = left.m02;
			b = left.m12;
			c = left.m22;
			d = left.m32;
			break;
		case 3:
			a = left.m03;
			b = left.m13;
			c = left.m23;
			d = left.m33;
			break;
		}
		switch (rowtarget) {
		case 0:
			left.m00 += a * factor;
			left.m10 += b * factor;
			left.m20 += c * factor;
			left.m30 += d * factor;
			break;
		case 1:
			left.m01 += a * factor;
			left.m11 += b * factor;
			left.m21 += c * factor;
			left.m31 += d * factor;
			break;
		case 2:
			left.m02 += a * factor;
			left.m12 += b * factor;
			left.m22 += c * factor;
			left.m32 += d * factor;
			break;
		case 3:
			left.m03 += a * factor;
			left.m13 += b * factor;
			left.m23 += c * factor;
			left.m33 += d * factor;
			break;
		}
	}
	private static void rowCombine(Matrix3f left, Matrix3f right, int rowsrc, int rowtarget, float factor) {
		rowCombine(left, rowsrc, rowtarget, factor);
		rowCombine(right, rowsrc, rowtarget, factor);
	}
	private static void rowCombine(Matrix3f left, int rowsrc, int rowtarget, float factor) {
		float a=0,b=0,c=0,d=0;
		switch (rowsrc) {
		case 0:
			a = left.m00;
			b = left.m10;
			c = left.m20;
			break;
		case 1:
			a = left.m01;
			b = left.m11;
			c = left.m21;
			break;
		case 2:
			a = left.m02;
			b = left.m12;
			c = left.m22;
			break;
		}
			switch (rowtarget) {
			case 0:
				left.m00 += a * factor;
				left.m10 += b * factor;
				left.m20 += c * factor;
				break;
			case 1:
				left.m01 += a * factor;
				left.m11 += b * factor;
				left.m21 += c * factor;
				break;
			case 2:
				left.m02 += a * factor;
				left.m12 += b * factor;
				left.m22 += c * factor;
				break;
			}
		}
	}
