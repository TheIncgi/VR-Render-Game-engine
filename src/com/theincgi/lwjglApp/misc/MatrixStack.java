package com.theincgi.lwjglApp.misc;

import java.util.ArrayList;

import org.lwjgl.util.vector.Matrix4f;

import com.theincgi.lwjglApp.render.Location;

public class MatrixStack implements AutoCloseable{
	public static MatrixStack modelViewStack = new MatrixStack();
	public static MatrixStack projection = new MatrixStack();
	
	private ArrayList<Matrix4f> stack = new ArrayList<Matrix4f>();
	private int top = 0;
	
	public MatrixStack() {
		stack.add(new Matrix4f());
		stack.get(0).setIdentity();
	}
	
	public MatrixStack push() {
		if(stack.size()==top+1) {
			Matrix4f tmp;
			stack.add(tmp = new Matrix4f());
			tmp.load(stack.get(top++));
			return this;
		}
		Matrix4f tmp = stack.get(top+1);
		tmp.load(stack.get(top++));
		return this;
	}
	public MatrixStack pushTransform(Location transform) {
		push();
		transform.applyTo(get());
		return this;
	}
	public Matrix4f get() {
		return stack.get(top);
	}
	public Matrix4f pop() {
		try{
			return get();
		}finally{
			top--;
			if(top<0)Logger.preferedLogger.e("MatrixStack", "Too many pops", new RuntimeException());
		}
	}
	public void reset() {
		if(top>0)Logger.preferedLogger.e("MatrixStack", "Too few pops", new RuntimeException());
		top = 0;
		get().setIdentity();
	}
	@Override
	public void close() {
		pop();
	}
}
