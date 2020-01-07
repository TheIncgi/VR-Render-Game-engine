package com.theincgi.lwjglApp.render;

import java.nio.FloatBuffer;
import java.util.Arrays;

import org.lwjgl.opengl.GL45;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import com.theincgi.lwjglApp.Utils;
import com.theincgi.lwjglApp.render.shaders.ShaderProgram;

public class Location {
    public final float[] pos,rot;
    //Matrix4f matrix;
    public Location() {
        this(0,0,0);
    }

    public Location(float x, float y, float z) {
        this(x,y,z,0,0,0);
    }

    public Location(float x, float y, float z, float yaw, float pitch, float roll) {
//        pos[0] = x;
//        this.y = y;
//        this.z = z;
//        this.yaw = yaw;
//        this.pitch = pitch;
//        this.roll = roll;
//        
    	pos = new float[] {x, y, z};
    	rot = new float[] {yaw, pitch, roll};
        //matrix = new Matrix4f();
    }

    public void move(float dx, float dy, float dz){
        pos[0] += dx;
        pos[1] += dy;
        pos[2] += dz;
    }
    public void rotate(float dYaw, float dPitch, float dRoll){
        rot[0] +=dYaw;
        rot[1] += dPitch;
        rot[2] += dRoll;
    }

    public void setPos(float x, float y, float z){
        pos[0] = x;
        pos[1] = y;
        pos[2] = z;

    }
    public void setPos(Vector3f v) {
    	pos[0] = v.x;
    	pos[1] = v.y;
    	pos[2] = v.z;
	}
    public void setRotation(float yaw, float pitch, float roll){
        rot[0] = yaw;
        rot[1] = pitch;
        rot[2] = roll;
    }

    public void setX(float x) {
        pos[0] = x;
    }

    public void setY(float y) {
    	pos[1] = y;
    }

    public void setZ(float z) {
        pos[2] = z;
    }

    public void setYaw(float yaw) {
        rot[0] = yaw;
    }

    public void setPitch(float pitch) {
        rot[1] = pitch;
    }

    public void setRoll(float roll) {
        rot[2] = roll;
    }

    public float getX() {
        return pos[0];
    }

    public float getY() {
        return pos[1];
    }

    public float getZ() {
        return pos[2];
    }

    public float getYaw() {
        return rot[0];
    }

    public float getPitch() {
        return rot[1];
    }

    public float getRoll() {
        return rot[2];
    }

    public Matrix4f applyTo(Matrix4f mat) {
    	mat.translate(new Vector3f(getX(), getY(), getZ()));
    	mat.rotate((float) Math.toRadians(getRoll()), Utils.AXIS_OUT);
    	mat.rotate((float) Math.toRadians(getPitch()), Utils.AXIS_RIGHT);
    	mat.rotate((float) Math.toRadians(getYaw()), Utils.AXIS_UP);
    	return mat;
    }
    
    
    //copy xyz data into given array
    public void setPos(float[] pos){
        pos[0] = this.pos[0];
        pos[1] = this.pos[1];
        pos[2] = this.pos[2];
        if(pos.length>3)
            pos[3] = 1;
    }
    
    

    private static float interpolate(float a, float b, float f){
        return a*(1-f) + b*f;
    }
    public static void interpolate(Location result, Location a, Location b, float factor){
        result.pos[0] = interpolate(a.pos[0], b.pos[0], factor);
        result.pos[1] = interpolate(a.pos[1], b.pos[1], factor);
        result.pos[2] = interpolate(a.pos[2], b.pos[2], factor);
        result.rot[0] = interpolate(a.rot[0], b.rot[0], factor);
        result.rot[1] = interpolate(a.rot[1], b.rot[1], factor);
        result.rot[2] = interpolate(a.rot[2], b.rot[2], factor);
    }

    @Override
    public Location clone(){
        return new Location(pos[0], pos[1], pos[2], rot[0], rot[1], rot[2]);
    }
    public void cloneTo(Location location){
        location.pos[0] = pos[0];
        location.pos[1] = pos[1];
        location.pos[2] = pos[2];
        location.rot[0] = rot[0];
        location.rot[1] = rot[1];
        location.rot[2] = rot[2];
    }

	
    @Override
    public String toString() {
    	StringBuilder b = new StringBuilder();
    	b.append("Location: <Pos: ");
    	b.append(Arrays.toString(pos));
    	b.append("> <Rot: ");
    	b.append(Arrays.toString(rot));
    	b.append(">");
    	return b.toString();
    }


}