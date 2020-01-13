#version 450
layout(location=0)out vec4 FragColor;
layout(location=1)out vec4 EmissionColor;


in vec3 vertexPosition;
in vec2 texturePosition;
in vec3 normalPosition;

void main(){
	FragColor = vec4(1,0,0,1);
	EmissionColor = vec4(1,0,0,1);
}