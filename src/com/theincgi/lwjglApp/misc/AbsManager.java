package com.theincgi.lwjglApp.misc;

import java.lang.ref.Cleaner;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Consumer;

public abstract class AbsManager<S, T> {
	public static final Cleaner cleaner = Cleaner.create();
	
	private WeakHashMap<S, T> cache = new WeakHashMap<>();
	
	public Optional<T> get(S name){
		return Optional.ofNullable(cache.computeIfAbsent(name, this::_load));
	}	

	public final void forLoaded(Consumer<T> each) {
		for(T t : cache.values()) {
			each.accept(t);
		}
	}
	
	private T _load(S key) {
		T t = load(key);
		if(t!=null)
			cleaner.register(t, ()->onUnload(t));
		return t;
	}
	protected abstract T load(S key);
	
	protected abstract void onUnload(T t);
}
