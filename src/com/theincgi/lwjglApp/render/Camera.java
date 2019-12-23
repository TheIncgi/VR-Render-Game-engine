package com.theincgi.lwjglApp.render;


import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL45;
import org.lwjgl.util.vector.Matrix4f;

import com.theincgi.lwjglApp.Launcher;
import com.theincgi.lwjglApp.misc.MatrixStack;
import com.theincgi.lwjglApp.misc.Pair;
import com.theincgi.lwjglApp.render.shaders.ShaderProgram;

public class Camera {
    Location location;
    public float fov = 110, near = .5f, far = 100;
    
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
    
    public void loadProjectionMatrix(){
		Pair<Integer, Integer> bufferSize = Launcher.getMainWindow().getBufferSize();
		float width =bufferSize.x;
		float height = bufferSize.y;
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