#version 450
//out vec4 FragColor;
layout(location=0)out vec4 FragColor;
layout(location=1)out vec4 EmissionColor;
in vec2 UV;
uniform sampler2D renderedTexture;
uniform sampler2D emissionTexture;
uniform float uptime;


void main() {

	FragColor     = texture( renderedTexture, UV) + texture( emissionTexture, UV);// + near(emissionTexture, uv);//texture(emissionTexture, UV + 0.025*vec2( sin(uptime+1024.0*UV.x),cos(uptime+768.0*UV.y)) );
	EmissionColor = texture( emissionTexture, UV);
	//FragColor = vec4(UV.xy,0,1);
}