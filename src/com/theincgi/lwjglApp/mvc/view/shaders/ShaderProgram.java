package com.theincgi.lwjglApp.mvc.view.shaders;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opengl.GL20.*;

import com.theincgi.lwjglApp.misc.Logger;

public class ShaderProgram {
	static Logger log = Logger.consoleLogger;
	
	private int programHandle;
	private String label;
	
	
	
	public void use() {
		if(programHandle==0)
			log.w("ShaderProgram#use", "No program handle exists for '"+label+"'");
		glUseProgram(programHandle);
	}
	
	public static int loadShader( int type, File srcFile ) {
		try(FileInputStream fis = new FileInputStream(srcFile)){
			return loadShader(type, fis);
		} catch (FileNotFoundException e) {
			log.e("ShaderProgram#loadShader", e);
		} catch (IOException e) {
			log.e("ShaderProgram#loadShader", e);
		}
		return 0;
	}
	public static int loadShader( int type, InputStream in) {
		String src;
		try {
			src = new String(in.readAllBytes());
			return loadShader( type, src );
		} catch (IOException e) {
			log.e("ShaderProgram#loadShader", "Unable to read all bytes", e);
			return 0;
		}
	}
	private static int loadShader(int type, String src) {
		int shader = glCreateShader(type);
		log.checkGL();
		glShaderSource(shader, src);
		log.checkGL();
		glCompileShader(shader);
		log.checkGL();
		try(MemoryStack stack = MemoryStack.stackPush()){
			IntBuffer status = stack.mallocInt(1);
			glGetShaderiv(shader, GL_COMPILE_STATUS, status);
			if(status.get(0) == GL_FALSE) {
				log.d("ShaderProgram#loadShader", "src: "+src);
				log.w("ShaderProgram#loadShader", "Error compiling shader ("+status.get(0)+"): "+
				  glGetShaderInfoLog(shader));
				glDeleteShader(shader);
				return 0;
			}
		}
		return shader;
	}
}
