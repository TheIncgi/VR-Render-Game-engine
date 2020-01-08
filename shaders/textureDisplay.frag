#version 450
//out vec4 FragColor;
layout(location=0)out vec4 FragColor;
in vec2 UV;
uniform sampler2D renderedTexture;
uniform float uptime;

void main() {
	vec2 uv = UV;
	// if( abs(uv.x -.5) > .45 || abs(uv.y -.5) > .45){
	// 	float s = 8;
	// 	FragColor = vec4(floor(uv.x*s)/s,floor(uv.y*s)/s,0,1);

	// }else if(abs(.5-uv.x)<.001 || abs(.5-uv.y)<.002){
	// 	FragColor = texture( renderedTexture, uv)/(1.4);
	// }else{
	//FragColor = texture( renderedTexture, UV + 0.0025*vec2( sin(uptime+1024.0*UV.x),cos(uptime+768.0*UV.y)) );
		FragColor = texture( renderedTexture, uv);

	//}
	//FragColor = vec4(UV.x,UV.y,0,1);
}