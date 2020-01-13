#version 450

layout (location = 0) in vec3 vPosition;
layout (location = 1) in vec2 texPosition;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelViewMatrix;

uniform float scale;
uniform vec2 tileSize;
uniform vec2 vecNextChar;
uniform vec2 vecNextLine;
uniform vec2 cursor;
uniform float leading;
uniform float pxpcm; //pixels per centimeter
uniform int fontFlags;

out vec2 texturePosition;

bool checkFlag(int flag, int bit){
	return ((flag>>bit) & 1) == 1;
}

void main(){
	vec2 vscale = tileSize/tileSize.y *scale;
	mat4 transform = mat4( //Opengl Uses column-major matrix ordering, so the transpose part is on the bottom instead
		vscale.x,                 0,     0,              0,
		       0,          vscale.y,     0,       		 0,
		       0,                 0,     1,              0,
/*tx*/	cursor.x/tileSize.x*vecNextChar.x*scale/2+(vPosition.y==0 &&checkFlag(fontFlags, 1)?vecNextChar.x/3*scale:0), 
/*ty*/ -cursor.y/tileSize.y/pxpcm *2,
/*tz*/	       0,
/* 1*/         1
	);
	
	gl_Position = projectionMatrix * viewMatrix * (modelViewMatrix * transform)  * vec4(vPosition,1);// + vec4(cursor.x/100,0,0,0);
	texturePosition = vec2(texPosition.x, 1-texPosition.y);
}