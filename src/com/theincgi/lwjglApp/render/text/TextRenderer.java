package com.theincgi.lwjglApp.render.text;

import java.util.Optional;
import java.util.PrimitiveIterator.OfInt;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.theincgi.lwjglApp.misc.Logger;
import com.theincgi.lwjglApp.misc.MatrixStack;
import com.theincgi.lwjglApp.render.Model;
import com.theincgi.lwjglApp.render.ObjManager;
import com.theincgi.lwjglApp.render.shaders.ShaderManager;
import com.theincgi.lwjglApp.render.shaders.ShaderProgram;
import com.theincgi.lwjglApp.ui.Color;


public class TextRenderer {
	public static final Vector2f RIGHT= new Vector2f(.01f, 0), DOWN = new Vector2f(0, -.01f);
	private static final char FORMATCHAR  	= '§';
	public static final int BOLD_ON    		= 'B';
	public static final int ITALIC_ON  		= 'I';
	public static final int UNDERLINE_ON	= 'U';
	public static final int STRIKETHRU_ON	= 'S';
	public static final int BOLD_OFF    	= 'b';
	public static final int ITALIC_OFF  	= 'i';
	public static final int UNDERLINE_OFF	= 'u';
	public static final int STRIKETHRU_OFF	= 's';
	public static final int COLOR      		= 'C'; //r,g,b<,a>; reads till ;
	
	private static Optional<Model> textTile = Optional.empty();
	/**Tab size measured in tile widths (as codepoint 32 may or may not be included)*/
	public static int tabSize = 2;
	
	/**OpenGL Context required to load an object model*/
	public static void init() {
		textTile = ObjManager.INSTANCE.get("cmodels/plane/1cm_square.obj", "fontRenderer");
	}
	
	
	private static final int PLAIN = 0, BOLD = 1, ITALICS = 2, STRIKETHRU = 4, UNDERLINE = 8;
	
	public static Vector2f renderText(FontTexture ft, String text, boolean useFormattingCodes, float heightInCentimeters) {
		return renderText(ft, text, RIGHT, DOWN, null, useFormattingCodes, heightInCentimeters);
	}
	/**
	 * Renders text in the relative XY plane using the provided font texture.<br>
	 * Everything is relative to the local origin
	 * @param ft The font to render the text with
	 * @param text The text to render, escaped chars are allowed
	 * @param vecNextChar The relative direction to place chars on the same line
	 * @param vecNextLine The relative direction to place the next line
	 * @param cursor nullable cursor starting position
	 * @param useFormaters If the format char should be used to change font colors and styles
	 * @param heightInCentimeters defines how to scale the overall render. Scaling is linear
	 * 
	 * @return Returns the end point where the cursor left off
	 * */
	public static Vector2f renderText(FontTexture ft, CharSequence text, Vector2f vecNextChar, Vector2f vecNextLine, Vector2f cursor, boolean useFormaters, float heightInCentimeters) {
		cursor = cursor==null? new Vector2f() : cursor;
		if(textTile.isEmpty()) return cursor; //nothing to draw with
		int fontFlags = 0;
		Optional<ShaderProgram> fontRenderer = textTile.get().shader;
		if(fontRenderer.isEmpty()) return cursor;
		
		ShaderProgram shader = fontRenderer.get();		
		shader.bind();
		shader.trySetMatrix("projectionMatrix",MatrixStack.projection.get());
		shader.trySetMatrix("modelViewMatrix", MatrixStack.modelViewStack.get());
		shader.trySetUniform("fontFlags", fontFlags);
		shader.trySetUniform("tileSize", new float[]{ft.getTileWidth(), ft.getTileHeight()});
		shader.trySetUniform("scale", heightInCentimeters);
		shader.trySetUniform("leading", ft.getLeading());
		shader.trySetUniform("color", Color.WHITE.vec());
		shader.trySetUniform("vecNextChar", new float[]{vecNextChar.x, vecNextChar.y});
		shader.trySetUniform("vecNextLine", new float[]{vecNextLine.x, vecNextLine.y});
		shader.trySetUniform("pxpcm", PXPCM);
		//shader.trySetUniform("cursor",      new float[]{cursor.x,      vecNextLine.y});
		shader.trySetUniformTexture("plainTex", ft.getTexPlain().getHandle(), 0);
		shader.trySetUniformTexture("boldTex",  ft.getTexBold().getHandle(), 1);
		
		StringBuffer valBuf = new StringBuffer();
		
		ShaderProgram sp = fontRenderer.get();
		OfInt it = text.codePoints().iterator();
		if(it.hasNext()) for(int cp = 0; it.hasNext();) {
			cp=it.nextInt();
			switch(cp) {
				case '\n':
					cursor.x = 0;
					cursor.y += ft.getTileHeight(); //same as ascent+descent here
					break;
				case '\t':
					cursor.x += ft.getTileWidth()*2;
					break;
				case FORMATCHAR:{ //§
					if(useFormaters) {
						if(!it.hasNext()) break;
						int cmd = it.nextInt();
						switch (cmd) {
							case BOLD_ON:
								fontFlags |= BOLD;
								shader.trySetUniform("fontFlags", fontFlags); break;
							case BOLD_OFF:
								fontFlags &= ~BOLD;
								shader.trySetUniform("fontFlags", fontFlags); break;
							case ITALIC_ON:
								fontFlags |= ITALICS;
								shader.trySetUniform("fontFlags", fontFlags); break;
							case ITALIC_OFF:
								fontFlags &= ~ITALICS;
								shader.trySetUniform("fontFlags", fontFlags); break;
							case STRIKETHRU_ON:
								fontFlags |= STRIKETHRU;
								shader.trySetUniform("fontFlags", fontFlags); break;
							case STRIKETHRU_OFF:
								fontFlags &= ~STRIKETHRU;
								shader.trySetUniform("fontFlags", fontFlags); break;
							case UNDERLINE_ON:
								fontFlags |= UNDERLINE;
								shader.trySetUniform("fontFlags", fontFlags); break;
							case UNDERLINE_OFF:
								fontFlags &= ~UNDERLINE;
								shader.trySetUniform("fontFlags", fontFlags); break;
							case COLOR:{
								valBuf.setLength(0);
								Color color = new Color(1, 1, 1);
								int ind = 0;
								int x = 0;
								while(it.hasNext() && x!=';') {
									x = it.nextInt();
									if(x==',' || x==';') {
										switch (ind++) {
										case 0:
											color.setR(Float.parseFloat(valBuf.toString())); break; //TODO optimize Float.parseFloat, many values will be the same on each frame anyway, dynamic compute
										case 1:
											color.setG(Float.parseFloat(valBuf.toString())); break;
										case 2:
											color.setB(Float.parseFloat(valBuf.toString())); break;
										case 3:
											color.setA(Float.parseFloat(valBuf.toString())); break;
										default:
											Logger.preferedLogger.w("TextRender", "Invalid number of color components in text::\n"+text);
										}
										valBuf.setLength(0);
									}else
										valBuf.appendCodePoint(x);
								}
								if(ind==3)
									color.setA(1);
								shader.trySetUniform("color", color.vec());
							}
						default:
							break;
						}
						break;
					}//dont break not using formatters | else if use format
				}//dont break not using formatters | end of case statment for §
				default:
					shader.bind(); //unbound by model draw
					shader.trySetUniform("cursor", new float[] {cursor.x, cursor.y});
					Vector4f uv = ft.getUV(cp);
					shader.trySetUniform("uv", new float[] {uv.x, uv.y, uv.z, uv.w});
					textTile.ifPresent(t->t.drawAtOrigin());
					cursor.x+=ft.getCharWidth(cp);
			}
			
		}
		return cursor;
	}

	/**Pixels per centimeter*/
	public static final float PXPCM = 20;
	/**
	 * Returns the amout of pixel space used to draw all the text (not making a texture)<br>
	 * Used to help transform text before rendering<br>
	 * @return A vector containing the width and height in centimeters assuming {@link TextRenderer#PXPCM}px per centimeter;
	 * */
	public static Vector2f measureArea(FontTexture ft, String text, Vector2f vecNextChar, Vector2f vecNextLine, boolean ignoreFormats) {
		if(ignoreFormats)
			text = text.replaceAll("[^§]§(?:(?:[BIUSbius])|(?:C(?:[\\d,\\.]+);))", "");
		String[] lines = text.split("\\n");
		int max = 0;
		for (String string : lines) {
			max = Math.max(max, measureLine(ft, string));
		}
		Vector2f out =  new Vector2f(max, lines.length*ft.getTileHeight());
		return (Vector2f) out.scale(1/PXPCM); 
	}

	/**Returns the width of a line ignoring any formating chars or \n<br>
	 * \t is replaced with " " * tabsize*/
	public static int measureLine(FontTexture ft, String text) {
		int w = 0;
		text.replace("\t", " ".repeat(tabSize));
		for (int i = 0; i < text.length(); i++) {
			int cp = text.codePointAt(i);
			w += ft.getCharWidth(w);
		}
		return w;
	}

	
}
