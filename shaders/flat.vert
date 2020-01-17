#version 450

layout (location = 0) in vec3 vPosition;
uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

void main(){
	vec4 tmp = projectionMatrix * viewMatrix * modelViewMatrix * vec4(vPosition,1);
	gl_Position = tmp;
}