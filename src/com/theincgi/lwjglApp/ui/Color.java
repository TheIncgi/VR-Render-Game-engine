package com.theincgi.lwjglApp.ui;

import static com.theincgi.lwjglApp.Utils.inRangeI;
import static com.theincgi.lwjglApp.Utils.inRangeE;
import static java.lang.Math.abs;

import java.util.Arrays;

import com.theincgi.lwjglApp.Utils;

public class Color implements Cloneable{
	public static final Color WHITE = new Color(1, 1, 1).lock(),
							  BLACK = new Color(0, 0, 0).lock(),
							  RED   = new Color(1, 0, 0).lock(),
							  GREEN	= new Color(0, 1, 0).lock(),
							  BLUE  = new Color(0, 0, 1).lock(),
							  YELLOW= new Color(1, 1, 0).lock(),
							  SKY   = new Color(0, 1, 1).lock(),
							  PURPLE= new Color(1, 0, 1).lock(),
							  LIGHT_GRAY = fromHSV(0, 0, .75f),
							  GRAY       = fromHSV(0, 0, .50f),
							  DARK_GRAY  = fromHSV(0, 0, .25f);
	
	
	private float[] rgba = new float[4];
	private boolean isMutable = true;
	
	public Color(float r, float g, float b) {
		this(r,g,b, 1.0f);
	}

	
	public Color(float r, float g, float b, float a) {
		this.rgba[0] = r;
		this.rgba[1] = g;
		this.rgba[2] = b;
		this.rgba[3] = a;
		check();
	}
	
	public static Color fromHex(int argb) {
		return new Color((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, (argb >> 24) & 0xFF);
	}
	
	public static Color fromHSV(float h, float s, float v) { return fromHSV(h,s,v,1);}
	public static Color fromHSV(float h, float s, float v, float a) {
		Color x = new Color(0, 0, 0);
		x.setHSV(h, s, v, a);
		return x;
	}
	
	public void setHSV(float h, float s, float v, float a) {
		if(!isMutable) throw new ImmutableColorException();
		float c = v*s;
		float x = c*(1-abs(h/60)%2-1);
		float m = v-c;
		
		if(inRangeE(h, 0f, 60f)) {
			rgba[0] = c;
			rgba[1] = x;
			rgba[2] = 0;
		}else if(inRangeE(h, 0f, 120f)) {
			rgba[0] = x;
			rgba[1] = c;
			rgba[2] = 0;
		}else if(inRangeE(h, 120f, 180f)) {
			rgba[0] = 0;
			rgba[1] = c;
			rgba[2] = x;
		}else if(inRangeE(h, 180f, 240f)) {
			rgba[0] = 0;
			rgba[1] = x;
			rgba[2] = c;
		}else if(inRangeE(h, 240f, 300f)) {
			rgba[0] = x;
			rgba[1] = 0;
			rgba[2] = c;
		}else{ //300 to 360 
			rgba[0] = c;
			rgba[1] = 0;
			rgba[2] = x;
		}
		rgba[0] += m;
		rgba[1] += m;
		rgba[2] += m;
		check();
	}
	
	public void setRGB(float r, float g, float b) {
		if(!isMutable) throw new ImmutableColorException();
		this.rgba[0] = r; this.rgba[1] = g; this.rgba[2] = b;
		check();
	}
	public void setRGBA(float r, float g, float b, float a) {
		if(!isMutable) throw new ImmutableColorException();
		setRGB(r, g, b); this.rgba[3] = a;
		check();
	}
	
	public float getHue() {
		float cmax = Utils.max(rgba[0], rgba[1], rgba[2]);
		float cmin = Utils.min(rgba[0], rgba[1], rgba[2]);
		float delta = cmax-cmin;
		if(delta == 0)
			return 0;
		else if(cmax==r())
			return 60*( (rgba[1]-rgba[2])/delta %6 );
		else if(cmax==g())
			return 60*( (rgba[2]-r())/delta + 2 );
		else // cmax==b
			return 60*( (r()-rgba[1])/delta + 4 );
	}
	public float getSaturation() {
		float cmax = Utils.max(rgba[0], rgba[1], rgba[2]);
		if(cmax == 0) return 0;
		float cmin = Utils.min(rgba[0], rgba[1], rgba[2]);
		float delta = cmax-cmin;
		return delta / cmax;
	}
	public float getValue() {
		return Utils.max(rgba[0], rgba[1], rgba[2]);
	}
	
	
	
	public float r() {
		return rgba[0];
	}


	public void setR(float r) {
		if(!isMutable) throw new ImmutableColorException();
		this.rgba[0] = r;
	}


	
	public float g() {
		return rgba[1];
	}


	public void setG(float g) {
		if(!isMutable) throw new ImmutableColorException();
		this.rgba[1] = g;
	}


	
	public float b() {
		return rgba[2];
	}


	public void setB(float b) {
		if(!isMutable) throw new ImmutableColorException();
		this.rgba[2] = b;
	}


	public float a() {
		return rgba[3];
	}


	public void setA(float a) {
		if(!isMutable) throw new ImmutableColorException();
		this.rgba[3] = a;
	}


	private final void check() {
		if(!inRangeI(r(), 0f, 1f)) throw new InvalidColorException(this);
		if(!inRangeI(g(), 0f, 1f)) throw new InvalidColorException(this);
		if(!inRangeI(b(), 0f, 1f)) throw new InvalidColorException(this);
		if(!inRangeI(a(), 0f, 1f)) throw new InvalidColorException(this);
	}
	
	@Override
	public Color clone(){
		return new Color(r(), g(), b(), a());
	}
	public Color lock() {
		isMutable = false; //not cloned intentionaly
		return this;
	}
	public boolean isMutable() {
		return isMutable;
	}
	
	public static class InvalidColorException extends RuntimeException {
		private static final long serialVersionUID = 5288139105423209314L;
		public InvalidColorException(Color c) {
			super(String.format("Color range exceeded (%9.4f, %9.4f, %9.4f, %9.4f)[RGBA]", c.r(),c.g(),c.b(),c.a()));
		}
	}
	public static class ImmutableColorException extends RuntimeException {
		private static final long serialVersionUID = 1723885586789025696L;
		public ImmutableColorException() {
			super("Color is marked immutable");
		}
	}
	public float[] vec() {
		return rgba;
	}

	public int getARGB() {
		return (((int)(a()*255))<<24) | (((int)(r()*255))<<16) | (((int)(g()*255))<<8) | (((int)(b()*255)));
	}
	
	@Override
	public String toString() {
		return "Color: "+Arrays.toString(rgba);
	}

}
