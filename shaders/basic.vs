#version 450

layout (location = 0) in vec3 vPosition;

uniform vec3 cameraPos;
uniform vec3 cameraRot;
uniform vec3 objectPos;
uniform vec3 objectRot;


uniform float uptime;

out vec4 vertexColor;

void main(){
	vec3 tmp = vec3(.1 * sin(uptime)+vPosition.x, vPosition.yz);

	gl_Position = vec4(tmp, 1.0);
	float z = .5*sin(uptime)+0.5;
	vertexColor = vec4(z,z,z, 1.0);
}