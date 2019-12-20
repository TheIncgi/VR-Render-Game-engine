package com.theincgi.lwjglApp.misc;

import static org.lwjgl.opengl.GL45.*;

public class Logger {
	public Logger() {}
	
	public static final Logger consoleLogger = new Logger();
	
	/**Crash the app if a gl error occurs*/
	public static boolean strictGL = true;
	
	private Object[] last = new Object[4];
	
	public void i(String tag, String debugMsg) {
		if(matchesLast("INFO", tag, debugMsg))
			incrementLast();
		else
			System.out.printf("[INFO]  %30s | %s\n", tag, debugMsg);
	}
	
	public void d(String tag, String debugMsg) {
		if(matchesLast("DEBUG", tag, debugMsg))
			incrementLast();
		else
			System.out.printf("[DEBUG] %30s | %s\n", tag, debugMsg);
	}
	public void w(String tag, String warningMessage) {
		if(matchesLast("WARN", tag, warningMessage))
			incrementLast();
		else
			System.err.printf("[WARN]  %30s | %s\n", tag, warningMessage);
	}
	/**Debug Warning, added because notification about a missing key for a shader could be handy if you are debuging the shader
	 * but not handy if it's intended*/
	public void sd(String tag, String warningMessage) {
		if(matchesLast("SHADER DEBUG", tag, warningMessage))
			incrementLast();
		else
			System.err.printf("[SDEBUG]%30s | %s\n", tag, warningMessage);
	}
	public void e(String tag, Throwable er) {
		if(matchesLast("ER", tag, er))
			incrementLast();
		else {
			System.err.printf("[ERROR] %30s | ", tag);
			er.printStackTrace();
		}
	}
	public void e(String tag, String msg, Throwable er) {
		if(matchesLast("ER", tag, msg, er))
			incrementLast();
		else {
			System.err.printf("[ERROR] %30s | %s | ",msg,  tag);
			er.printStackTrace();
		}
	}
	
	public void checkGL() {
		int code = glGetError();
		if(code!=GL_NO_ERROR) {
			throw new GL_Exception(pickMessage(code));
		}
	}
	
	private static String pickMessage(int code){
        switch (code){
            case GL_INVALID_ENUM: return "GL Invalid Enum";
            case GL_INVALID_VALUE: return "GL Invalid Value";
            case GL_INVALID_OPERATION: return "GL Invalid Operation";
            //case GL20.GL_INVALID_FRAMEBUFFER_OPERATION: return "GL Invalid Framebuffer Operation";
            case GL_STACK_OVERFLOW: return "GL Stack Overflow";
            case GL_STACK_UNDERFLOW: return "GL Stack Underflow";
            case GL_OUT_OF_MEMORY: return "GL Out of Memory";
            default: return "GL Undefined Error ("+code+")";
        }
    }
	
	public boolean incrementLast() {
		return false; //not implemented for console
	}
	
	private boolean matchesLast(Object... args) {
		if(args.length!=last.length) return false;
		
		for(int i = 0; i<last.length; i++) {
			if(!args[i].equals(last.length)) return false;
		}
		return true;
	}
}
