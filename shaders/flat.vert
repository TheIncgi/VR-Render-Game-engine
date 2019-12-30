#version 450

layout (location = 0) in vec3 vPosition;
uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;

void main(){
	gl_Position = projectionMatrix  * modelViewMatrix * vec4(vPosition,1);
}