package com.theincgi.lwjglApp.misc;

import java.util.Optional;

import org.lwjgl.util.vector.Vector4f;

import com.theincgi.lwjglApp.render.Drawable;

public class RayCast {
	public Vector4f worldOffset, rayDirection;
	/**The hit location from the ray, the value of result.w may be either 0 or 1*/
	public Optional<Vector4f> result = Optional.empty();
	/**If the result is set, the raycastedObject should be set by the scene handling the raytrace op*/
	public Optional<Drawable> raycastedObject = Optional.empty();
	
	public RayCast(Vector4f worldOffset, Vector4f rayDirection) {
		this.worldOffset = worldOffset;
		this.rayDirection = rayDirection;
	}

	
	public void setShortResult(Vector4f altV) {
		result.ifPresentOrElse(theResult->{
			float current = Vector4f.sub(theResult, worldOffset, new Vector4f()).length();
			if( current > Vector4f.sub(altV, worldOffset, new Vector4f()).length()) {
				result = Optional.of(altV);
			}	
		}, ()->{
			result = Optional.of(altV);
		});
	}
	public void setLongResult(Vector4f altV) {
		result.ifPresentOrElse(theResult->{
			float current = Vector4f.sub(theResult, worldOffset, new Vector4f()).length();
			if( current < Vector4f.sub(altV, worldOffset, new Vector4f()).length()) {
				result = Optional.of(altV);
			}	
		}, ()->{
			result = Optional.of(altV);
		});
	}
}
