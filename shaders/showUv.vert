#version 450

layout (location = 0) in vec3 vPosition;
layout (location = 1) in vec2 tPosition;
layout (location = 2) in vec3 nPosition;

out vec3 vertex;
out vec2 uv;
out vec3 norm;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;

void main(){
	gl_Position = projectionMatrix  * modelViewMatrix * vec4(vPosition,1);
	vertex = vPosition;
	uv = tPosition;
	norm = normalize(nPosition);
}