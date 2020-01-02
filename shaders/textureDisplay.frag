#version 450
//out vec4 FragColor;
layout(location=0)out vec4 FragColor;

in vec2 UV;
uniform sampler2D renderedTexture;
uniform float uptime;

void main() {
	//FragColor = vec4(UV.x, UV.y, 0,);//texture( renderedTexture, UV + 0.005*vec2( sin(uptime+1024.0*UV.x),cos(uptime+768.0*UV.y)) ).xyz;
	FragColor = vec4(1,.5,0,1);
}