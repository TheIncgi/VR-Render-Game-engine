package com.theincgi.lwjglApp.vrGUI;

import java.util.Optional;
import java.util.function.Consumer;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.theincgi.lwjglApp.Launcher;
import com.theincgi.lwjglApp.misc.Logger;
import com.theincgi.lwjglApp.misc.MatrixStack;
import com.theincgi.lwjglApp.misc.Tickable;
import com.theincgi.lwjglApp.mvc.models.AABB;
import com.theincgi.lwjglApp.mvc.models.Bounds;
import com.theincgi.lwjglApp.mvc.models.Colideable;
import com.theincgi.lwjglApp.mvc.models.OrientedBoundingBox;
import com.theincgi.lwjglApp.render.Drawable;
import com.theincgi.lwjglApp.render.ImgTexture;
import com.theincgi.lwjglApp.render.Material;
import com.theincgi.lwjglApp.render.MaterialGroup;
import com.theincgi.lwjglApp.render.Model;
import com.theincgi.lwjglApp.render.ObjManager;
import com.theincgi.lwjglApp.render.text.FontTexture;
import com.theincgi.lwjglApp.render.text.TextRenderer;
import com.theincgi.lwjglApp.ui.Color;
import com.theincgi.lwjglApp.ui.Scene;

public class Button implements Colideable, Tickable {
	Optional<String> text = Optional.empty();
	//Optional<ImgTexture> icon = Optional.empty();
	Optional<Consumer<Boolean>> onPress = Optional.empty(),
			onRelease = Optional.empty();
	Optional<Model> buttonModel = Optional.empty();
	boolean isPressed = false;
	boolean isLocked = false;
	/**Position in a gui*/
	public final Vector2f buttonPosition = new Vector2f();
	protected float pushAmount = .005f; //.0052 thick for standard models
	Size size;
	private Optional<FontTexture> fontTexture;
	public float fontSize = 1;
	private Gui gui;
	private OrientedBoundingBox bounds;
	private boolean showBounds;
	
	
	public Button(Optional<FontTexture> defaultFont, String text, Size size) {
		this.fontTexture = defaultFont;
		this.size = size;
		this.text = Optional.ofNullable(text);
		if(size!=null)
			buttonModel = ObjManager.INSTANCE.get(size.getModelName(), "full");
		this.bounds = size.getBounds().clone();
		bounds.setParent(this);
	}
	public Button(ImgTexture icon, Size size) {
		this(Optional.ofNullable(icon), size);
	}
	public Button(Optional<ImgTexture> icon, Size size) {
		this.size = size;
		setImage(icon);
		if(size!=null)
			buttonModel = ObjManager.INSTANCE.get(size.getModelName(), "full");
		this.bounds = size.getBounds().clone();
		bounds.setParent(this);
	}

	public void setImage(Optional<ImgTexture> icon) {
		//this.icon = icon;
		buttonModel.ifPresent(mod->{
			mod.getMaterial().ifPresent(matg->{
				mod.makeMaterialUnique();
				Material mat = matg.materials.get("image");
				if(mat==null) return;
				mat.map_kd = icon; //diffuse map
			});
		});
	}
	public void setButtonColor(Color color) {
		buttonModel.ifPresent(mod->{
			mod.getMaterial().ifPresent(matg->{
				mod.makeMaterialUnique();
				Material mat = matg.materials.get("image");
				if(mat!=null);
					mat.kd = color; //diffuse color
				mat = matg.materials.get("white"); //name of the material for the edges
				if(mat!=null)
					mat.kd = color;
			});
		});
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
		//Launcher.getMainWindow().vrControllers
		return false;
	}
	
	//
	@Override
	public void draw() {
		buttonModel.ifPresent(model->{
			try(MatrixStack ms = MatrixStack.modelViewStack.push(new Vector3f(buttonPosition.x, buttonPosition.y, isPressed?-pushAmount:0))){
				model.drawAtOrigin();
				text.ifPresent(txt->{
					if(fontTexture==null) return;
					if(fontTexture.isEmpty()) return;
					Vector2f area = TextRenderer.measureArea(fontTexture.get(), txt);
					area.scale(-.5f);
					try(MatrixStack m2 = MatrixStack.modelViewStack.push(new Vector3f(area.x, area.y, 0))){
						TextRenderer.renderText(fontTexture.get(), txt, fontSize);
					}
				});
			}
		});
		
		if(bounds!=null) {
			bounds.setTransformFrom(MatrixStack.modelViewStack.get());
			bounds.draw();
		}
	}
	
	@Override
	public boolean showBounds() {
		return showBounds;
	}
	@Override
	public void setShowBounds(boolean show) {
		showBounds = show;
	}
	
	@Override
	public boolean isTransparent() {
		return false;
	}
	@Override
	public float[] getTransparentObjectPos() {
		return null;
	}
	@Override
	public Bounds getBounds() {
		return bounds;
	}
	
	public float getWidth() {
		if(size!=null)
			return size.getWidth();
		Logger.preferedLogger.w("Button#getWidth", "Looks like the button class may have been overriden but getWidth hasn't been or null was passed in for a button size");			
		return 0;
	}
	public float getHeight() {
		if(size!=null)
			return size.getHeight();
		Logger.preferedLogger.w("Button#getHeight", "Looks like the button class may have been overriden but getHeight hasn't been or null was passed in for a button size");			
		return 0;
	}

	// 0.045 - large dim
	// 0.02 - small dim
	public enum Size{						/*                 (           origin                  )               (     x+ width     )             (      y+ height   )                 z+          */
		s5x5(  "cmodels/buttons/5x5.obj",   .05f, .05f, new OrientedBoundingBox(new Vector4f(-0.020f, -0.020f, 0.0f, 1),  0.04f, 0.04f, 0.0052f)),
		s5x10( "cmodels/buttons/5x10.obj",  .05f, .10f, new OrientedBoundingBox(new Vector4f(-0.020f, -0.045f, 0.0f, 1),  0.04f, 0.09f, 0.0052f)),
		s10x5( "cmodels/buttons/10x5.obj",  .10f, .05f, new OrientedBoundingBox(new Vector4f(-0.045f, -0.020f, 0.0f, 1),  0.09f, 0.04f, 0.0052f)),
		s10x10("cmodels/buttons/10x10.obj", .10f, .10f, new OrientedBoundingBox(new Vector4f(-0.045f, -0.045f, 0.0f, 1),  0.09f, 0.09f, 0.0052f));
		
		private final String modelName;
		float width, height;
		private OrientedBoundingBox bounds;
		private Size(String model, float width, float height, OrientedBoundingBox bounds) {
			this.bounds = bounds;
			modelName = model;
			this.width = width;
			this.height = height;
		}
		public String getModelName() {
			return modelName;
		}
		public float getWidth() {
			return width;
		}
		public float getHeight() {
			return height;
		}
		public OrientedBoundingBox getBounds() {
			return bounds;
		}
	}

	public void setGui(Gui gui) {
		this.gui = gui;
	}
	@Override
	public boolean allowRaytraceHits() {
		return true;
	}
}
