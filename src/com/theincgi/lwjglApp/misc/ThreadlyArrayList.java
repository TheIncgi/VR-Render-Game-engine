package com.theincgi.lwjglApp.misc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class ThreadlyArrayList<T> {
	private ArrayList<T> theList = new ArrayList<>();
	private Queue<T> toAdd = new LinkedList<>();
	private Queue<T> toRemove = new LinkedList<>();
	
	public ThreadlyArrayList() {
	}
	
	public void add(T item) {
		synchronized (toAdd) {
			toAdd.add(item);
		}
	}
	public void remove(T item) {
		synchronized (toRemove) {
			toRemove.add(item);
		}
	}
	public void 
}
