package com.theincgi.lwjglApp.vrTests;

import java.util.Optional;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import com.theincgi.lwjglApp.Launcher;
import com.theincgi.lwjglApp.misc.Pair;
import com.theincgi.lwjglApp.render.Side;
import com.theincgi.lwjglApp.render.TextureManager;
import com.theincgi.lwjglApp.render.text.FontTexture;
import com.theincgi.lwjglApp.render.text.FontTextures;
import com.theincgi.lwjglApp.render.vr.VRController;
import com.theincgi.lwjglApp.ui.Scene;
import com.theincgi.lwjglApp.vrGUI.Button;
import com.theincgi.lwjglApp.vrGUI.Gui;

public class TestGui extends Gui{
	Optional<FontTexture> defaultFont;
	/**Creates the test gui at the pointing pointing position of the controller + a litle distance*/
	public TestGui(Scene scene) {
		super(scene, .05f * 3, .05f*3);
		defaultFont = FontTextures.INSTANCE.get(new Pair<String, Integer>("ascii_consolas", 64));
		Button b;
		addButton(b = new Button(defaultFont, "§C1,0,1;OK", Button.Size.s5x5), 0, 0);
		b.setOnPress(e->{
			b.setText(e?"\n\nVery\n§C0,1,0;oki":"\n\nstill\n§C1,0,1;oki");
		});
		b.setImage(TextureManager.INSTANCE.get("img/testIcon.png"));
	}
	
	
	
	public void open(Side summonSide) {
		location.setIdentity();
		VRController vrController = Launcher.getMainWindow().vrControllers;
		Matrix4f.mul(location, summonSide.isLeft()? vrController.getLeftTransform() : vrController.getRightTransform(), location);
		location.translate(new Vector3f(0,0,-.05f)); //5cm buffer
		location.rotate((float)Math.toRadians(-45), new Vector3f(1, 0 ,0));
		super.open();
	}
	
	public void toggle(Side summonSide) {
		if(isOpen())
			close();
		else
			open(summonSide);
	}
}
