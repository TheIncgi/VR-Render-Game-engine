package com.theincgi.lwjglApp.render;


import com.theincgi.lwjglApp.render.shaders.ShaderProgram;

public class Camera {
    Location location;

    public Camera() {
        this(0,0,0);
    }
    public Camera(float x, float y, float z) {
        this(x, y, z, 0, -45, 0 );
    }
    public Camera(float x, float y, float z, float yaw, float pitch, float roll) {
        location = new Location(x, y, z, yaw, pitch, roll);

    }

    public Location getLocation() {
        return location;
    }
    
    /**Pass camera location, rotation*/
    public void tellShader(ShaderProgram sp) {
    	sp.trySetUniform("cameraPos", location.pos);
    	sp.trySetUniform("cameraRot", location.rot);
    }
}