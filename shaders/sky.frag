#version 450
layout(location=0)out vec4 FragColor;
layout(location=1)out vec4 EmissionColor;


in vec3 vertexPosition;
in vec2 texturePosition;
in vec3 normalPosition;

uniform vec3 sunPos;
uniform vec3 sunColor;
uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;
uniform vec3 cameraPos;
uniform float uptime;

// Mode 		Desc
// 0	Color on and Ambient off
// 1	Color on and Ambient on
// 2	Highlight on
// 3	Reflection on and Ray trace on
// 4	Transparency: Glass on
// 			 Reflection: Ray trace on
// 5	Reflection: Fresnel on and Ray trace on
// 6	Transparency: Refraction on
// 					 Reflection: Fresnel off and Ray trace on
// 7	Transparency: Refraction on
// 			 	  	 Reflection: Fresnel on and Ray trace on
// 8	Reflection on and Ray trace off
// 9	Transparency: Glass on
// 					 Reflection: Ray trace off
// 10	Casts shadows onto invisible surfaces
uniform int  material_illum; 
uniform vec4 material_kd; //diffuse
uniform vec4 material_ka; //ambient
uniform vec4 material_ks; //specular
uniform vec4 material_ke; //emmission
uniform vec4 material_tf; //transmission
uniform float material_d;  //disolve
uniform int material_halo; //boolean disolve -halo
uniform float material_ns; //specular exponent
uniform float material_ni; //IOR
uniform sampler2D map_ka;  //ambient map
uniform sampler2D map_kd;  //diffuse map
uniform sampler2D map_ks;  //specular map
uniform sampler2D map_ns;  //specular exponent map
uniform sampler2D map_d;   //disolve map
uniform sampler2D map_disp; //displacement map
uniform sampler2D map_bump; //normal map
uniform int used_textures_flag;

const int KD_USED = 0;
const int KA_USED = 1;
const int KS_USED = 2;
const int NS_USED = 3;
const int D_USED = 4;
const int DISP_USED = 5;
const int BUMP_USED = 6;

bool checkFlag(int flag, int bit){
	return ((flag>>bit) & 1) == 1;
}

void main() {
	if(checkFlag(used_textures_flag, KD_USED)){
		FragColor = texture(map_kd, texturePosition);
	}else{
		FragColor = material_kd;
	}
	EmissionColor = vec4(0,0,0,1);
}