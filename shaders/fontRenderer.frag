#version 450
layout(location=0)out vec4 FragColor;
layout(location=1)out vec4 EmissiveColor;

in vec2 texturePosition;
uniform sampler2D plainTex;
uniform sampler2D boldTex;
uniform float uptime;
uniform vec4 color;
uniform int fontFlags;
uniform vec2 cursor;
uniform vec4 uv;

bool checkFlag(int flag, int bit){
	return ((flag>>bit) & 1) == 1;
}

vec2 map(vec2 x, vec2 inMin, vec2 inMax, vec2 outMin, vec2 outMax) {
  return (x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
}

void main() {
		//if(checkFlag(fontFlags, 1))
		FragColor = color*texture( checkFlag(fontFlags, 1)?plainTex:boldTex, map(texturePosition, vec2(0,0), vec2(1,1), uv.xy, uv.zw));
		EmissiveColor = vec4(0,1,1,1);
}