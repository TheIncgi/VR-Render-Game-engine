#version 450
out vec4 FragColor;
in vec4 vertexColor;

uniform vec3 objectPos;
uniform vec3 sunPos;

void main() {
	float dist = distance(objectPos, sunPos);
	vec4 tmp = vertexColor;
	if(dist < 9)
		tmp = vec4(1,0,0,1);
	FragColor = tmp;
}