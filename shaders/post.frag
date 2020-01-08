#version 450
//out vec4 FragColor;
layout(location=0)out vec4 FragColor;
in vec2 UV;
uniform sampler2D renderedTexture;
uniform sampler2D emissionTexture;
uniform float uptime;

void main() {
	vec2 uv = UV;
	FragColor = texture( renderedTexture, UV);// + 0.0025*vec2( sin(uptime+1024.0*UV.x),cos(uptime+768.0*UV.y)) );
}