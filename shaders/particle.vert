version 450

layout (location = 0) in vec3 vPosition;
layout (location = 1) in vec2 texPosition;
layout (location = 2) in vec3 normPosition;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelViewMatrix;

uniform vec3 particlePosition;
uniform vec4 particleColor;
uniform vec3 particleScale;
uniform vec3 particleVelocity;


out vec3 vertexPosition;
out vec2 texturePosition;
out vec3 normalPosition; 

void main(){
	gl_Position = projectionMatrix * viewMatrix * (modelViewMatrix * transform)  * vec4(vPosition,1);// + vec4(cursor.x/100,0,0,0);
	texturePosition = vec2(texPosition.x, 1-texPosition.y);
	vertexPosition = vPosition; 
	normalPosition = normPosition;
}