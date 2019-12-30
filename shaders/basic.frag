#version 450
out vec4 FragColor;
in vec4 vertexColor;

in vec3 vertexPosition;
in vec2 texturePosition;
in vec3 normalPosition;

uniform vec3 sunPos;
uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;
uniform vec3 cameraPos;
uniform float uptime;

void main() {
	vec3 worldPos = ( modelViewMatrix * vec4(vertexPosition, 1.0)).xyz;
	vec3 worldNormal = normalize((modelViewMatrix * vec4(normalPosition, 0.0)).xyz); //1.0?
	vec3 lightVector = normalize( sunPos - worldPos );
	vec3 lightReflect = reflect( lightVector,  worldNormal);

	float brightness = dot(worldNormal, vec3(1,0,0));//, lightVector);//worldNormal, lightVector);
	brightness = ((brightness-1) *.5) +1;
	FragColor = vertexColor * brightness;

	FragColor = vec4(normalize(worldPos)/2+.5, 1);
}