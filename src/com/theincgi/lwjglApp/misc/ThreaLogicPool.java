package com.theincgi.lwjglApp.misc;
import java.io.Closeable;

public class ThreaLogicPool implements Closeable{

	
	Worker[] workers;
	public ThreaLogicPool(Runnable task, int threads) {
		workers = new Worker[threads];
		for (int i = 0; i < workers.length; i++) {
			workers[i] = new Worker(task);
		}
	}
	
	public void runAndWait() {
		for(int i = 0; i<workers.length; i++) 
			workers[i].doWork();
		for(int i = 0; i<workers.length; i++) {
			workers[i].join();
		}
			
	}
	
	
	
	public static class Worker{
		Thread thread;
		Runnable task;
		private volatile boolean running = false;
		private volatile boolean complete = false;
		
		public Worker(Runnable task) {
			this.task = task;
			thread = new Thread(this::_task);
			thread.start();
		}
		
		private void _task() {
			while(!complete ) {
				while(!running && !complete)
					try {
						Thread.sleep(Long.MAX_VALUE);
					} catch (InterruptedException e) {}
				if(!complete)
					synchronized (this) {
						task.run();
						running = false;
						notifyAll();
					}
			}
		}
		
		public void doWork() {
			synchronized (this) {
				running = true;
				thread.interrupt();
			}
		}
		
		public void join() {
			synchronized (this) { //if this is first, it waits, if it's second, it isn't running
				while(running)
					try {
						wait();
					} catch (InterruptedException e) {}
			}
		}
		public boolean isRunning() {
			return running;
		}
		
		public void markComplete() {
			complete = true;
		}
	}



	public void close() {
		for (int i = 0; i < workers.length; i++) {
			workers[i].markComplete();
			workers[i].thread.interrupt();
		}
	}
}
