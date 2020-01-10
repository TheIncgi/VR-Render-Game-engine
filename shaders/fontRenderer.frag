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

const float strikethruThickness = .04;
const float underlineThickness = .1;


bool checkFlag(int flag, int bit){
	return ((flag>>bit) & 1) == 1;
}

vec2 map(vec2 x, vec2 inMin, vec2 inMax, vec2 outMin, vec2 outMax) {
  return (x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
}

void main() {
	vec4  BLACK = vec4(0,0,0,color.a);
	vec4  WHITE = vec4(1,1,1,color.a);
	//if(checkFlag(fontFlags, 0))
	vec4 textColor = color*texture( checkFlag(fontFlags, 0)?boldTex:plainTex, map(texturePosition, vec2(0,0), vec2(1,1), uv.xy, uv.zw));
	textColor = ((checkFlag(fontFlags, 2) && abs(.5-texturePosition.y)<strikethruThickness)? BLACK : textColor);
	FragColor = ((checkFlag(fontFlags, 3) && 1-texturePosition.y<underlineThickness)? BLACK : textColor);
	EmissiveColor = vec4(0,1,1,1);
}