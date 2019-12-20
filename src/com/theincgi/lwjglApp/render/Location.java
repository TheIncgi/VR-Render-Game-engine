package com.theincgi.lwjglApp.render;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL45;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import com.theincgi.lwjglApp.Utils;

public class Location {
    protected float x,y,z,yaw,pitch,roll;
    //Matrix4f matrix;
    public Location() {
        this(0,0,0);
    }

    public Location(float x, float y, float z) {
        this(x,y,z,0,0,0);
    }

    public Location(float x, float y, float z, float yaw, float pitch, float roll) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
        //matrix = new Matrix4f();
    }

    public void move(float dx, float dy, float dz){
        this.x += dx;
        this.y += dy;
        this.z += dz;
    }
    public void rotate(float dYaw, float dPitch, float dRoll){
        this.yaw +=dYaw;
        this.pitch += dPitch;
        this.roll += dRoll;
    }

    public void setPos(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;

    }
    public void setRotation(float yaw, float pitch, float roll){
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public float getRoll() {
        return roll;
    }

    //copy xyz data into given array
    public void putPos(float[] pos){
        pos[0] = x;
        pos[1] = y;
        pos[2] = z;
        if(pos.length>3)
            pos[3] = 1;
    }

//    public Matrix4f getMatrix(){
//    	return matrix;
//    }
//    public void updateMatrix() {
//    	matrix.setIdentity();
//
//        matrix.rotate(getRoll(), Utils.AXIS_OUT);
//        matrix.rotate(getPitch(), Utils.AXIS_RIGHT);
//        matrix.rotate(getYaw(),   Utils.AXIS_UP);
//        Vector3f pos = new Vector3f(x, y, z);
//        matrix.translate(pos);
//    }
//
//
//    public void glLoadMatrix() {
//    	try (MemoryStack stack = MemoryStack.stackPush()){
//    		FloatBuffer fb = stack.mallocFloat(16);
//    		matrix.store(fb);
//    		GL45.glLoadMatrixf(fb);
//    	}
//    }
    
    public void apply() {
    	GL45.glRotatef(roll, 0, 0, 1);
    	GL45.glRotatef(pitch, 1, 0, 0);
    	GL45.glRotatef(yaw, 0, 1, 0);
    	GL45.glTranslatef(x, y, z);
    }
    

    private float interpolate(float a, float b, float f){
        return a*(1-f) + b*f;
    }
    public void interpolate(Location result, Location a, Location b, float factor){
        result.x = interpolate(a.x, b.x, factor);
        result.y = interpolate(a.y, b.y, factor);
        result.z = interpolate(a.z, b.z, factor);
        result.yaw = interpolate(a.yaw, b.yaw, factor);
        result.pitch = interpolate(a.pitch, b.pitch, factor);
        result.roll = interpolate(a.roll, b.roll, factor);
    }

    @Override
    public Location clone(){
        return new Location(x, y, z, yaw, pitch, roll);
    }
    public void cloneTo(Location location){
        location.x = x;
        location.y = y;
        location.z = z;
        location.yaw = yaw;
        location.pitch = pitch;
        location.roll = roll;
    }
    


}