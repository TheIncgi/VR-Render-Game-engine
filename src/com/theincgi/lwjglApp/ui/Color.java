package com.theincgi.lwjglApp.ui;

import static com.theincgi.lwjglApp.Utils.inRange;
import static java.lang.Math.abs;

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
							  LIGHT_GRAY = fromHSV(0, 0, .75),
							  GRAY       = fromHSV(0, 0, .50),
							  DARK_GRAY  = fromHSV(0, 0, .25);
	
	
	private float r,g,b,a;
	private boolean isMutable;
	
	public Color(double r, double g, double b) {
		this(r,g,b, 1.0);
	}


	public Color(double r, double g, double b, double a) {
		this.r = (float) r;
		this.g = (float) g;
		this.b = (float) b;
		this.a = (float) a;
		check();
	}
	
	public static Color fromHex(int argb) {
		return new Color((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, (argb >> 24) & 0xFF);
	}
	
	public static Color fromHSV(double h, double s, double v) { return fromHSV(h,s,v,1);}
	public static Color fromHSV(double h, double s, double v, double a) {
		Color x = new Color(0, 0, 0);
		x.setHSV(h, s, v, a);
		return x;
	}
	
	public void setHSV(double h, double s, double v, double a) {
		if(!isMutable) throw new ImmutableColorException();
		double c = v*s;
		double x = c*(1-abs(h/60)%2-1);
		double m = v-c;
		
		if(inRange(h, 0, 60)) {
			r = (float) c;
			g = (float) x;
			b = 0;
		}else if(inRange(h, 0, 120)) {
			r = (float) x;
			g = (float) c;
			b = 0;
		}else if(inRange(h, 120, 180)) {
			r = 0;
			g = (float) c;
			b = (float) x;
		}else if(inRange(h, 180, 240)) {
			r = 0;
			g = (float) x;
			b = (float) c;
		}else if(inRange(h, 240, 300)) {
			r = (float) x;
			g = 0;
			b = (float) c;
		}else{ //300 to 360 
			r = (float) c;
			g = 0;
			b = (float) x;
		}
		r += m;
		g += m;
		b += m;
		check();
	}
	
	public void setRGB(float r, float g, float b) {
		if(!isMutable) throw new ImmutableColorException();
		this.r = r; this.g = g; this.b = b;
		check();
	}
	public void setRGBA(float r, float g, float b, float a) {
		if(!isMutable) throw new ImmutableColorException();
		setRGB(r, g, b); this.a = a;
		check();
	}
	
	public float getHue() {
		float cmax = Utils.max(r, g, b);
		float cmin = Utils.min(r, g, b);
		float delta = cmax-cmin;
		if(delta == 0)
			return 0;
		else if(cmax==r)
			return 60*( (g-b)/delta %6 );
		else if(cmax==g)
			return 60*( (b-r)/delta + 2 );
		else // cmax==b
			return 60*( (r-g)/delta + 4 );
	}
	public float getSaturation() {
		float cmax = Utils.max(r, g, b);
		if(cmax == 0) return 0;
		float cmin = Utils.min(r, g, b);
		float delta = cmax-cmin;
		return delta / cmax;
	}
	public float getValue() {
		return Utils.max(r, g, b);
	}
	
	
	public float getR() {
		return r;
	}
	public float r() {
		return r;
	}


	public void setR(float r) {
		if(!isMutable) throw new ImmutableColorException();
		this.r = r;
	}


	public float getG() {
		return g;
	}
	public float g() {
		return g;
	}


	public void setG(float g) {
		if(!isMutable) throw new ImmutableColorException();
		this.g = g;
	}


	public float getB() {
		return b;
	}
	public float b() {
		return b;
	}


	public void setB(float b) {
		if(!isMutable) throw new ImmutableColorException();
		this.b = b;
	}


	public float getA() {
		return a;
	}
	public float a() {
		return a;
	}


	public void setA(float a) {
		if(!isMutable) throw new ImmutableColorException();
		this.a = a;
	}


	private final void check() {
		if(!inRange(r, 0, 1)) throw new InvalidColorException(this);
		if(!inRange(g, 0, 1)) throw new InvalidColorException(this);
		if(!inRange(b, 0, 1)) throw new InvalidColorException(this);
		if(!inRange(a, 0, 1)) throw new InvalidColorException(this);
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return new Color(r, g, b, a);
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
			super(String.format("Color range exceeded (%9.4f, %9.4f, %9.4f, %9.4f)[RGBA]", c.r,c.g,c.b,c.a));
		}
	}
	public static class ImmutableColorException extends RuntimeException {
		private static final long serialVersionUID = 1723885586789025696L;
		public ImmutableColorException() {
			super("Color is marked immutable");
		}
	}
}
