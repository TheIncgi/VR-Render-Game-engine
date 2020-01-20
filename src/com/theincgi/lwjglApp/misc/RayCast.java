package com.theincgi.lwjglApp.misc;

import java.util.Optional;

import org.lwjgl.util.vector.Vector4f;

import com.theincgi.lwjglApp.mvc.models.Bounds;
import com.theincgi.lwjglApp.mvc.models.Colideable;
import com.theincgi.lwjglApp.render.Drawable;

public class RayCast {
	public Vector4f worldOffset, rayDirection;
	/**The hit location from the ray, the value of result.w may be either 0 or 1*/
	public Optional<Vector4f> result = Optional.empty();
	/**If the result is set, the raycastBounds will contain the exact bounds object that the ray hit*/
	public Optional<Bounds> raycastedBounds = Optional.empty();
	
	public RayCast(Vector4f worldOffset, Vector4f rayDirection) {
		this.worldOffset = worldOffset;
		this.rayDirection = rayDirection;
	}

	
	public void setShortResult(Vector4f altV, Bounds bounds) {
		result.ifPresentOrElse(theResult->{
			float current = Vector4f.sub(theResult, worldOffset, new Vector4f()).length();
			if( current > Vector4f.sub(altV, worldOffset, new Vector4f()).length()) {
				result = Optional.of(altV);
				raycastedBounds = Optional.of(bounds);
			}	
		}, ()->{
			result = Optional.of(altV);
		});
	}
	public void setLongResult(Vector4f altV, Bounds bounds) {
		result.ifPresentOrElse(theResult->{
			float current = Vector4f.sub(theResult, worldOffset, new Vector4f()).length();
			if( current < Vector4f.sub(altV, worldOffset, new Vector4f()).length()) {
				result = Optional.of(altV);
				raycastedBounds = Optional.of(bounds);
			}	
		}, ()->{
			result = Optional.of(altV);
		});
	}
}