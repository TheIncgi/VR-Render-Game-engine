#version 450

layout (location = 0) in vec3 vPosition;
layout (location = 1) in vec2 texPosition;
uniform mat4 modelViewMatrix;
out vec2 UV;
void main(){
	gl_Position = modelViewMatrix * vec4(vPosition,1);
	UV = texPosition;
}