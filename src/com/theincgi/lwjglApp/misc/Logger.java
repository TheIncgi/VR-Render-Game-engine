package com.theincgi.lwjglApp.misc;

public class Logger {
	public Logger() {}
	
	public static final Logger consoleLogger = new Logger();
	
	
	private Object[] last = new Object[4];
	
	public void d(String tag, String debugMsg) {
		if(matchesLast("DEBUG", tag, debugMsg))
			incrementLast();
		else
			System.out.printf("%s30 | %s\n", tag, debugMsg);
	}
	public void w(String tag, String warningMessage) {
		if(matchesLast("WARN", tag, warningMessage))
			incrementLast();
		else
			System.err.printf("%s30 | %s\n", tag, warningMessage);
	}
	public void e(String tag, Throwable er) {
		if(matchesLast("ER", tag, er))
			incrementLast();
		else {
			System.err.printf("%s30 | ", tag);
			er.printStackTrace();
		}
	}
	public void e(String tag, String msg, Throwable er) {
		if(matchesLast("ER", tag, msg, er))
			incrementLast();
		else {
			System.err.printf("%s30 | %s | ",msg,  tag);
			er.printStackTrace();
		}
	}
	
	public void checkGL() {
		//TODO 
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
