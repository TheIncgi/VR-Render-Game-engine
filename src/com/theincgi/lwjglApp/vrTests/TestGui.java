package com.theincgi.lwjglApp.vrTests;

import java.util.Optional;

import com.theincgi.lwjglApp.misc.Pair;
import com.theincgi.lwjglApp.render.text.FontTexture;
import com.theincgi.lwjglApp.render.text.FontTextures;
import com.theincgi.lwjglApp.vrGUI.Button;
import com.theincgi.lwjglApp.vrGUI.Gui;

public class TestGui extends Gui{
	Optional<FontTexture> defaultFont;
	public TestGui() {
		super(.05f * 3, .05f*3);
		defaultFont = FontTextures.INSTANCE.get(new Pair<String, Integer>("consolas", 64));
		addButton(new Button(defaultFont, "OK", Button.Size.s5x5), 0, 0);
	}
}
