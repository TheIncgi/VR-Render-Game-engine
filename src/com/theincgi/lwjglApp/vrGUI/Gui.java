package com.theincgi.lwjglApp.vrGUI;

import java.util.ArrayList;
import java.util.Optional;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import com.theincgi.lwjglApp.misc.MatrixStack;
import com.theincgi.lwjglApp.misc.Tickable;
import com.theincgi.lwjglApp.mvc.models.Bounds;
import com.theincgi.lwjglApp.mvc.models.CompoundBounds;
import com.theincgi.lwjglApp.render.Drawable;
import com.theincgi.lwjglApp.render.Material;
import com.theincgi.lwjglApp.render.MaterialGroup;
import com.theincgi.lwjglApp.render.Model;
import com.theincgi.lwjglApp.render.ObjManager;
import com.theincgi.lwjglApp.ui.Scene;

public class Gui implements Drawable, Tickable{
	protected ArrayList<Drawable> guiElements = new ArrayList<>();
	public Matrix4f location = new Matrix4f();
	Optional<Model> square;
	private float guiWidth = 0, guiHeight = 0;
	protected CompoundBounds bounds = new CompoundBounds();
	private Scene scene;
	private boolean isOpen = false;
	public Gui(Scene scene, float widthMeters, float heightMeters) {
		this.scene = scene;
		square = ObjManager.INSTANCE.get("cmodels/plane/plane.obj", "full");
		guiWidth = widthMeters;
		guiHeight = heightMeters;
	}

	/***/
	public void addButton(Button button, int gridX, int gridY) {
		guiElements.add(button);
		button.setGui(this);
		button.buttonPosition.x = gridX * .05f;
		button.buttonPosition.y = gridY * .05f;
		bounds.addBounds(button.getBounds());
	}

	public Optional<Material> getBGMaterial() {
		if(square.isPresent()) {
			Model mod = square.get();
			mod.makeMaterialUnique();
			if(mod.getMaterial().isPresent()) {
				MaterialGroup mg = mod.getMaterial().get();
				return Optional.ofNullable(mg.materials.get("None"));
			}
		}
		return Optional.empty();
	}

	@Override
	public void draw() {
		try(MatrixStack m1 = MatrixStack.modelViewStack.pushTransform(location)){
			square.ifPresent(sq->{
				try(MatrixStack m2 = 
						MatrixStack.modelViewStack
						.pushTransform(new Matrix4f().scale(new Vector3f(guiWidth, guiHeight, 1)))){
					sq.drawAtOrigin();	
				}
			});
			for (Drawable b : guiElements) {
				b.draw();
			}
		}
	}

	public void open() {
		scene.addDrawable(this);
		scene.addTickable(this);
		isOpen = true;
	}
	public void close() {
		scene.removeDrawable(this);
		scene.removeTickable(this);
		isOpen = false;
	}
	
	public void toggle() {
		if(isOpen)
			close();
		else
			open();
	}
	
	public boolean isOpen() {
		return isOpen;
	}
	
	@Override
	public boolean onTickUpdate() {
		for (Drawable drawable : guiElements) {
			if(drawable instanceof Tickable)
				((Tickable) drawable).onTickUpdate();
		}
		return false;
	}

	@Override
	public boolean isTransparent() {
		return true;
	}

	@Override
	public float[] getTransparentObjectPos() {
		return new float[] {location.m30, location.m31, location.m32};
	}

	@Override
	public Optional<Bounds> getBounds() {
		return Optional.of(bounds);
	}
}
