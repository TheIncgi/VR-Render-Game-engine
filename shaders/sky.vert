#version 450

layout (location = 0) in vec3 vPosition;
layout (location = 1) in vec2 texPosition;
layout (location = 2) in vec3 normPosition;
uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform vec3 cameraPos;

uniform float uptime;


out vec3 vertexPosition;
out vec2 texturePosition;
out vec3 normalPosition; 

// //http://www.neilmendoza.com/glsl-rotation-about-an-arbitrary-axis/
// mat4 rotationMatrix(vec3 axis, float angle)
// {
//     axis = normalize(axis);
//     float s = sin(angle);
//     float c = cos(angle);
//     float oc = 1.0 - c;
    
//     return mat4(oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,  0.0,
//                 oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,  0.0,
//                 oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c,           0.0,
//                 0.0,                                0.0,                                0.0,                                1.0);
// }



void main(){
	mat4 model = mat4(
		1, 0, 0, 0,//cameraPos.x,
		0, 1, 0, 0,//cameraPos.y,
		0, 0, 1, 0,//cameraPos.z,
		0, 0, 0, 1
		); //x
	vec4 tmp = projectionMatrix * viewMatrix * model * vec4(vPosition,1);
	gl_Position = tmp;

	vertexPosition = vPosition; 
	texturePosition = texPosition;
	normalPosition = normPosition;
}


