#version 450

layout (location = 0) in vec3 vPosition;
layout (location = 1) in vec2 texPosition;
layout (location = 2) in vec3 normPosition;
uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;

uniform vec3 cameraPos;

uniform float uptime;

out vec4 vertexColor;

mat4 rotationMatrix(vec3 axis, float angle)
{
    axis = normalize(axis);
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1.0 - c;
    
    return mat4(oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,  0.0,
                oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,  0.0,
                oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c,           0.0,
                0.0,                                0.0,                                0.0,                                1.0);
}



void main(){
	vec4 tmp = rotationMatrix(vec3(0,1,0), uptime) * vec4(vPosition,1);
	tmp = vec4(tmp.xy,tmp.z-3, 1);//, .5 * sin(uptime)+vPosition.z -3);

	gl_Position = projectionMatrix  * modelViewMatrix * tmp;
	float z = .5*sin(uptime)+0.5;
	vertexColor = vec4(z,z,z, 1.0);
}


