package com.theincgi.lwjglApp.render.animation;

import java.util.ArrayList;

import com.theincgi.lwjglApp.misc.Logger;

public class Animation {

	private final Updater<?>[] updaters;
	private Long startTime;
	private long duration;
	private ArrayList<AnimationEventHandler> handlers;
	private boolean reverse;
	private Float progress;

	public Animation(Updater<?>...updaters) {
		this.updaters = updaters;
	}

	public synchronized void addListener(AnimationEventHandler handler) {
		if(handlers==null) handlers = new ArrayList<>();
		handlers.add(handler);
	}
	private synchronized void notifyListeners(AnimationEvent event) {
		if(handlers!=null)
		for (AnimationEventHandler animationEventHandler : handlers) {
			animationEventHandler.handleEvent(event);
		}
	}

	/**
	 * Sets the <code>after</code> param to start when this animation competes forward<br>
	 * This animation will be played if the after animation is played in reverse 
	 * @param after
	 * @return after argument
	 * */
	public synchronized Animation link(final Animation after) {
		after.addListener(e->{
			if(e.isFinRev())
				play();
		});
		this.addListener(e->{
			if(e.isFinFow())
				after.play();
		});
		return after;
	}

	/**Does nothing if playing*/
	public Animation setDuration(long milliseconds) {return setDuration(milliseconds, TimeUnit.MILLISECONDS);}
	/**Does nothing if playing
	 * @return */
	public synchronized Animation setDuration(long amount, TimeUnit unit) {
		if(startTime==null)
			duration = amount * unit.mult;
		return this;
	}
	public synchronized Animation setDuration(float amount, TimeUnit unit) {
		if(startTime==null)
			duration = (long) (amount * unit.mult);
		return this;
	}

	/**Does from progress left off if paused*/
	public synchronized void play() {
		if(duration==0) Logger.preferedLogger.w("Animation#play", "Duration of 0");
		if(progress!=null)
			resume();
		else {
			startTime = System.currentTimeMillis();
			reverse = false;
			notifyListeners(AnimationEvent.START_FORWARD);
		}
	}
	/**Does from progress left off if paused*/
	public synchronized void playReverse() {
		if(duration==0) Logger.preferedLogger.w("Animation#play", "Duration of 0");
		if(progress!=null)
			resume();
		else {
			startTime = System.currentTimeMillis();
			reverse = true;
			notifyListeners(AnimationEvent.START_REVERSE);
		}
	}
	/**You can change values for duration while paused if needed, will resume based on % complete*/
	public synchronized void pause() {
		progress = getProgress();
		startTime = null;
	}
	/**You can change values for duration while paused if needed, will resume based on % complete*/
	public synchronized void resume() {
		if(duration==0) Logger.preferedLogger.w("Animation#play", "Duration of 0");
		long now = System.currentTimeMillis();
		long passed = (long) (duration * progress);
		startTime = now-passed;
		progress = null;
	}
	public synchronized boolean isPaused() {
		return progress!=null;
	}
	/**Clear pause status to play from start or end*/
	public synchronized Animation reset() {
		progress = null; //mhmm, productive
		return this;
	}

	public synchronized boolean isPlaying() {
		return startTime!=null;
	}
	public synchronized void update() {
		if(startTime==null) return;
		float x = getProgress();
		if(reverse) x = 1-x;
		if(x==0 && reverse) {
			notifyListeners(AnimationEvent.FIN_REVERSE);
			startTime = null;
			for (int i = 0; i < updaters.length; i++) {
				updaters[i].updateStart();
			}
		}else if(x==1) {
			startTime = null;
			notifyListeners(AnimationEvent.FIN_FOWARD);
			for (int i = 0; i < updaters.length; i++) {
				updaters[i].updateStop();
			}
		}else {
			for (int i = 0; i < updaters.length; i++) {
				updaters[i].iupdate(x);
			}
		}
		
	}

	/**1 means it's approching the end of this stage, if playing backwards 1 is the start*/
	public synchronized float getProgress() {
		return Math.min(1, (System.currentTimeMillis()-startTime) / (float)duration );
	}

	public synchronized boolean isReverse() {
		return reverse;
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
		START_REVERSE, FIN_REVERSE,
		PAUSE, RESUME;
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
		private void updateStop() {
			update(stop);
		}
		private void updateStart() {
			update(start);
		}
		abstract public void update(S p);
	}

	abstract public static class Interpolator<S>{
		/**Avoid this for starting and stopping animations, fine in the middle*/
		public static final Interpolator<Float> LINEAR = new Interpolator<Float>() {
			@Override public Float interpolate(float x) {
				return x;
			}};
			public static final Interpolator<Float> SIGMOID = new Interpolator<Float>() {
				@Override public Float interpolate(float x) {
					return (float) (1/(1+Math.pow(Math.E, -((x-.5)*16))));
				}
			};

			public static Interpolator<Float> accelerate(float exitVelocity){
				return new Interpolator<Float>() {@Override
					public Float interpolate(float x) {
					return (float) Math.pow(x, exitVelocity); //Calculus!
				}
				};
			}
			public static Interpolator<Float> decelerate(float entryVelocity){
				return new Interpolator<Float>() {@Override
					public Float interpolate(float x) {
					return (float) Math.pow(1-x, entryVelocity); //Calculus!
				}
				};
			}

			abstract public S interpolate(float x);
	}

	@FunctionalInterface
	abstract public interface AnimationEventHandler {
		public void handleEvent(AnimationEvent event);
	}

}
