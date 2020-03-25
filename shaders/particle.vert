#version 450

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

uniform vec3 cameraPos;
uniform float uptime;


out vec3 vertexPosition;
out vec2 texturePosition;
out vec3 normalPosition; 

const float PI = 3.141459654;
const float HPI =1.57079827;

mat4 rotationX(float angle ) {
	return mat4(	1.0,		0,			0,			0,
			 		0, 	cos(angle),	-sin(angle),		0,
					0, 	sin(angle),	 cos(angle),		0,
					0, 			0,			  0, 		1);
}

mat4 rotationY(float angle ) {
	return mat4(	cos(angle),		0,		sin(angle),	0,
			 				0,		1.0,			 0,	0,
					-sin(angle),	0,		cos(angle),	0,
							0, 		0,				0,	1);
}

mat4 rotationZ(float angle ) {
	return mat4(	cos(angle),		-sin(angle),	0,	0,
			 		sin(angle),		cos(angle),		0,	0,
							0,				0,		1,	0,
							0,				0,		0,	1);
}
mat4 makeTransform(){
	return mat4(
		particleScale.x,				   0,						  0,			0,
		0,					 particleScale.y,						  0,			0,
		0,						 		   0,			particleScale.z,			0,
		0,								   0,						  0,			1
	);
}

void main(){
	mat4 transform = makeTransform();

	//yaw
	float dX = particlePosition.x - cameraPos.x;
	float dZ = particlePosition.z - cameraPos.z;
	float yaw = atan(dZ, dX);
	transform = transform * rotationY(yaw+HPI);

	//pitch
	float dist = distance(cameraPos, particlePosition);
	float dH = particlePosition.y - cameraPos.y;
	float pitch = asin(dH/dist);
	transform = transform * rotationX( -pitch );

	


	gl_Position = projectionMatrix * viewMatrix * (modelViewMatrix * transform )  * vec4(vPosition,1);// + vec4(cursor.x/100,0,0,0);
	texturePosition = vec2(texPosition.x, 1-texPosition.y);
	vertexPosition = vPosition; 
	normalPosition = normPosition;
}