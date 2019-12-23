#version 450

layout (location = 0) in vec3 vPosition;
uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;

uniform vec3 cameraPos;

uniform float uptime;

out vec4 vertexColor;

void main(){
	vec3 tmp = vec3(vPosition.xy, .5 * sin(uptime)+vPosition.z -3);

	gl_Position = projectionMatrix * modelViewMatrix * vec4(tmp, 1.0);
	float z = .5*sin(uptime)+0.5;
	vertexColor = vec4(z,z,z, 1.0);
}