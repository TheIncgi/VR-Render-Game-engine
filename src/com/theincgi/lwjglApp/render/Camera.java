package com.theincgi.lwjglApp.render;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import com.theincgi.lwjglApp.Utils;

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
    
    public void apply() {
    	location.apply();
    }

    public Location getLocation() {
        return location;
    }
}