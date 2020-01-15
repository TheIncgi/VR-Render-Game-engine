package com.theincgi.lwjglApp.vrGUI;

import java.util.Optional;

import com.theincgi.lwjglApp.Launcher;
import com.theincgi.lwjglApp.misc.Tickable;
import com.theincgi.lwjglApp.render.Drawable;
import com.theincgi.lwjglApp.render.ImgTexture;

public class Button implements Drawable, Tickable {
	Optional<String> text = Optional.empty();
	Optional<ImgTexture> icon = Optional.empty();
	Optional<Runnable> onPress = Optional.empty(),
			           onRelease = Optional.empty();
	
	public Button(String text) {
		this.text = Optional.ofNullable(text);
	}
	public Button(ImgTexture icon) {
		this.icon = Optional.ofNullable(icon);
	}
	public Button(Optional<ImgTexture> icon) {
		this.icon = icon;
	}
	
	public void setOnPress(Runnable onPress) {
		this.onPress = Optional.ofNullable(onPress);
	}
	public void setOnRelease(Runnable onRelease) {
		this.onPress = Optional.ofNullable(onRelease);
	}
	
	@Override
	public boolean onTickUpdate() {
		Launcher.getMainWindow().getVRWindow().ifPresent(vr->{
			vr.vrControllers.get
		});
	}
}
