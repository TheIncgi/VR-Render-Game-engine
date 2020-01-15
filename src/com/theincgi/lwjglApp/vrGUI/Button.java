package com.theincgi.lwjglApp.vrGUI;

import java.util.Optional;
import java.util.function.Consumer;

import com.theincgi.lwjglApp.Launcher;
import com.theincgi.lwjglApp.misc.Tickable;
import com.theincgi.lwjglApp.render.Drawable;
import com.theincgi.lwjglApp.render.ImgTexture;

public class Button implements Drawable, Tickable {
	Optional<String> text = Optional.empty();
	Optional<ImgTexture> icon = Optional.empty();
	Optional<Consumer<Boolean>> onPress = Optional.empty(),
			           			onRelease = Optional.empty();
	
	boolean isPressed = false;
	boolean isLocked = false;
	public Button(String text) {
		this.text = Optional.ofNullable(text);
	}
	public Button(ImgTexture icon) {
		this.icon = Optional.ofNullable(icon);
	}
	public Button(Optional<ImgTexture> icon) {
		this.icon = icon;
	}
	
	public void setOnPress(Consumer<Boolean> onPress) {
		this.onPress = Optional.ofNullable(onPress);
	}
	public void setOnRelease(Consumer<Boolean> onRelease) {
		this.onPress = Optional.ofNullable(onRelease);
	}
	
	public void setPressed(boolean pressedState, boolean triggerEvent) {
		isPressed = pressedState;
		if(triggerEvent)
			if(pressedState)
				onPress.ifPresent(op->op.accept(true));
			else
				onRelease.ifPresent(or->or.accept(false));
	}
	public void press() {
		setPressed(true, true);
	}
	public void unpress() {
		setPressed(false, true);
	}
	
	
	public void setLocked(boolean lockState) {
		isLocked = lockState;
	}
	public void lock() {
		setLocked(true);
	}
	public void unlock() {
		setLocked(false);
	}
	public boolean isLocked() {
		return isLocked;
	}
	
	@Override
	public boolean onTickUpdate() {
		Launcher.getMainWindow().getVRWindow().ifPresent(vr->{
			vr.vrControllers.getLeftPointingVector();
		});
	}
}
