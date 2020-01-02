#version 450
//out vec4 FragColor;
layout(location=0)out vec4 FragColor;
in vec2 UV;
uniform sampler2D renderedTexture;
uniform float uptime;

void main() {
	if( abs(UV.x -.5) > .45 || abs(UV.y -.5) > .45){
		FragColor = vec4(UV.x,UV.y,0,1);

	}else{
		FragColor = texture( renderedTexture, UV + 0.0025*vec2( sin(uptime+1024.0*UV.x),cos(uptime+768.0*UV.y)) );
		//FragColor = texture( renderedTexture, UV);

	}
	//FragColor = vec4(UV.x,UV.y,0,1);
}