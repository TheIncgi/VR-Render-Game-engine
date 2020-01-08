#version 450
layout(location=0)out vec4 FragColor;


in vec3 vertexPosition;
in vec2 texturePosition;
in vec3 normalPosition;

uniform vec3 sunPos;
uniform vec3 sunColor;
uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;
uniform vec3 cameraPos;
uniform float uptime;

void main() {
	vec3 worldPos = ( modelViewMatrix * vec4(vertexPosition, 1.0)).xyz;
	vec3 worldNormal = normalize(vec3(modelViewMatrix * ( vec4(normalPosition, 0.0))));
	vec3 cameraNormal = cameraPos - worldPos;
	vec3 lightVector = normalize( sunPos - worldPos );
	vec3 lightReflect = reflect( lightVector,  worldNormal);

	float brightness = dot(worldNormal, lightVector);//, lightVector);//worldNormal, lightVector);
	brightness = ((brightness-1) *.5) +1;

	float specular = clamp(-dot(cameraNormal, lightReflect), 0.0, 1.0);
	brightness *= 1+specular;

	FragColor = vec4(normalize(vertexPosition)/2+.5, 1) * brightness;
	//FragColor = vec4(normalize(normalPosition)*.5+.5, 1);//* brightness;
	//FragColor = vec4(brightness, brightness, brightness, 1);
	
}