package com.theincgi.lwjglApp.render.animation;

import java.util.ArrayList;
import java.util.function.Consumer;

import com.theincgi.lwjglApp.misc.Logger;
import com.theincgi.lwjglApp.misc.Tickable;
import com.theincgi.lwjglApp.ui.Scene;

public class Animation implements Tickable{

	private final Updater<?>[] updaters;
	private Long startTime;
	private long duration;
	private ArrayList<AnimationEventHandler> handlers;
	private boolean reverse = true;
	private boolean wasReverseBeforePause;
	private Float progress;
	private Scene scene;

	public Animation(Scene scene, Updater<?>...updaters) {
		this.scene = scene;
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
		reverse = false;
		if(progress!=null)
			resume();
		else {
			startTime = System.currentTimeMillis();
			notifyListeners(AnimationEvent.START_FORWARD);
		}
		scene.addTickable(this);
	}
	/**Does from progress left off if paused*/
	public synchronized void playReverse() {
		if(duration==0) Logger.preferedLogger.w("Animation#play", "Duration of 0");
		reverse = true;
		if(progress!=null)
			resume();
		else {
			startTime = System.currentTimeMillis();
			notifyListeners(AnimationEvent.START_REVERSE);
		}
		scene.addTickable(this);
	}
	public synchronized void playToggled() {
		if(reverse)
			play();
		else 
			playReverse();
	}
	
	/**You can change values for duration while paused if needed, will resume based on % complete*/
	public synchronized void pause() {
		if(startTime==null) return;
		progress = getProgress();
		startTime = null;
		wasReverseBeforePause = reverse;
	}
	/**You can change values for duration while paused if needed, will resume based on % complete*/
	public synchronized void resume() {
		if(progress==null) return;
		if(duration==0) Logger.preferedLogger.w("Animation#play", "Duration of 0");
		long now = System.currentTimeMillis();
		if(wasReverseBeforePause!=reverse)
			progress = 1-progress;
		long passed = (long) (duration * progress);
		startTime = now-passed;
		progress = null;
		scene.addTickable(this);
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
	@Override

	public synchronized boolean onTickUpdate() {
		if(startTime==null) return true;
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
			return false;
		}
		return true;
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
		Interpolator interpolator;
		public Updater(Interpolator i, S start, S stop) {
			this.interpolator = i;
			this.start = start;
			this.stop = stop;
		}
		private void iupdate(float f) {
			update(map(interpolator.interpolate(f)));
		}
		private void updateStop() {
			update(stop);
		}
		private void updateStart() {
			update(start);
		}
		abstract public void update(S p);
		abstract public S map(float x);
		
		public static Updater<Float> makeFloatUpdater(Animation.Interpolator interpolator, float start, float end, Consumer<Float> update){
			return new Updater<Float>(interpolator, start, end) {
				@Override
				public Float map(float x) {
					return (end-start)*x +start;
				}
				@Override
				public void update(Float p) {
					update.accept(p);
				}
			};
		};
	}

	abstract public static class Interpolator{
		/**Avoid this for starting and stopping animations, fine in the middle*/
		public static final Interpolator LINEAR = new Interpolator() {
			@Override public float interpolate(float x) {
				return x;
			}};
			public static final Interpolator SIGMOID = new Interpolator() {
				@Override public float interpolate(float x) {
					return (float) (1/(1+Math.pow(Math.E, -((x-.5)*16))));
				}
			};

			public static Interpolator accelerate(float exitVelocity){
				return new Interpolator() {@Override
					public float interpolate(float x) {
					return (float) Math.pow(x, exitVelocity); //Calculus!
				}
				};
			}
			public static Interpolator decelerate(float entryVelocity){
				return new Interpolator() {@Override
					public float interpolate(float x) {
					return (float) Math.pow(1-x, entryVelocity); //Calculus!
				}
				};
			}

			abstract public float interpolate(float x);
	}

	@FunctionalInterface
	abstract public interface AnimationEventHandler {
		public void handleEvent(AnimationEvent event);
	}

}
