package com.theincgi.lwjglApp.render.animation;

import java.util.ArrayList;

public class Animation {
	
	private Updater<?>[] updaters;
	private Long startTime;
	private long duration;
	private ArrayList<AnimationEventHandler> handlers;
	private boolean reverse;
	
	
	public Animation(Updater<?>...updaters) {
		this.updaters = updaters;
	}
	
	public synchronized void addListener(AnimationEventHandler handler) {
		if(handlers==null) handlers = new ArrayList<>();
		handlers.add(handler);
	}
	private synchronized void notifyListeners(AnimationEvent event) {
		for (AnimationEventHandler animationEventHandler : handlers) {
			animationEventHandler.handleEvent(event);
		}
	}
	
	/**Does nothing if playing*/
	public void setDuration(long milliseconds) {setDuration(milliseconds, TimeUnit.MILLISECONDS);}
	/**Does nothing if playing*/
	public void setDuration(long amount, TimeUnit unit) {
		if(startTime==null)
			duration = amount * unit.mult;
	}
	
	public synchronized void play() {
		startTime = System.currentTimeMillis();
		reverse = false;
		notifyListeners(AnimationEvent.START_FORWARD);
	}
	public synchronized void playReverse() {
		startTime = System.currentTimeMillis();
		reverse = true;
		notifyListeners(AnimationEvent.START_REVERSE);
	}
	public synchronized boolean isPlaying() {
		return startTime!=null;
	}
	public void update() {
		if(startTime==null) return;
		float x = Math.min(1, (System.currentTimeMillis()-startTime) / (float)duration );
		if(reverse) x = 1-x;
		for (int i = 0; i < updaters.length; i++) {
			updaters[i].iupdate(x);
		}
		if(x==0 && reverse) {
			notifyListeners(AnimationEvent.FIN_REVERSE);
			startTime = null;
		}else if(x==1) {
			startTime = null;
			notifyListeners(AnimationEvent.FIN_FOWARD);
		}
	}
	
	public enum TimeUnit{
		MILLISECONDS(1),
		SECONDS( 1_000),
		MINUTES(60_000),
		HOURS( 360_000);
		int mult;
		private TimeUnit(int mult) {
			this.mult = mult;
		}
	}
	
	public enum AnimationEvent{
		START_FORWARD, FIN_FOWARD,
		START_REVERSE, FIN_REVERSE;
		public boolean isFow() {
			return this.equals(FIN_FOWARD) || this.equals(START_FORWARD);
		}
		public boolean isRev() {
			return this.equals(FIN_REVERSE) || this.equals(START_REVERSE);
		}
		public boolean isStart() {
			return this.equals(START_REVERSE) || this.equals(START_REVERSE);
		}
		public boolean isFin() {
			return this.equals(FIN_FOWARD);
		}
		public boolean isStartFow() {
			return this.equals(START_FORWARD);
		}
		public boolean isStartRev() {
			return this.equals(START_REVERSE);
		}
		public boolean isFinFow() {
			return this.equals(FIN_FOWARD);
		}
		public boolean isFinRev() {
			return this.equals(FIN_REVERSE);
		}
	}
	
	abstract public static class Updater<S> {
		final S start, stop;
		Interpolator<? extends S> interpolator;
		public Updater(Interpolator<? extends S> i, S start, S stop) {
			this.interpolator = i;
			this.start = start;
			this.stop = stop;
		}
		private void iupdate(float f) {
			update(interpolator.interpolate(f));
		}
		abstract public void update(S p);
	}
	
	abstract public static class Interpolator<S>{
		abstract public S interpolate(float x);
	}
	
	@FunctionalInterface
	abstract public interface AnimationEventHandler {
		public void handleEvent(AnimationEvent event);
	}
}
