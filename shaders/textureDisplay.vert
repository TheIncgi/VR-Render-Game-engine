#version 450

layout (location = 0) in vec3 vPosition;
layout (location = 1) in vec2 texPosition;
out vec2 UV;
void main(){
	gl_Position = vec4(vPosition*.9,1);
	UV = texPosition;
}