package com.theincgi.lwjglApp.render;


import org.lwjgl.util.vector.Matrix4f;

import com.theincgi.lwjglApp.Launcher;
import com.theincgi.lwjglApp.misc.MatrixStack;
import com.theincgi.lwjglApp.render.shaders.ShaderProgram;

public class Camera {
    Location location;
    public float fov = 110, near = .1f, far = 35;
    
    public Camera() {
        this(0,0,0);
    }
    public Camera(float x, float y, float z) {
        this(x, y, z, 0, 0, 0 );
    }
    public Camera(float x, float y, float z, float yaw, float pitch, float roll) {
        location = new Location(x, y, z, yaw, pitch, roll);

    }

    public Location getLocation() {
        return location;
    }
    
    /**Pass camera location, rotation*/
    public void tellShader(ShaderProgram sp) {
    	if(sp!=null)
    		sp.trySetUniform("cameraPos", location.pos);
    }
    
    /**Does FOV and translation, applies on top of existing MatrixStack.projection top*/
    public void loadProjectionMatrix(){
		float width = Launcher.getMainWindow().getViewportWidth();
		float height = Launcher.getMainWindow().getViewportHeight();
		float aspectRatio = width / height;
		float yScale = (float) ((1f / Math.tan(Math.toRadians(fov / 2f))) * aspectRatio);
		float xScale = yScale / aspectRatio;
		float frustumLength = far - near;

		Matrix4f projectionMatrix = MatrixStack.projection.get();
		projectionMatrix.m00 = xScale;
		projectionMatrix.m11 = yScale;
		projectionMatrix.m22 = -((far + near) / frustumLength);
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -((2 * near * far) / frustumLength);
		projectionMatrix.m33 = 0;
		
		location.applyTo(projectionMatrix);
	}
    
}