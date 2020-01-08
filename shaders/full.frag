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
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ End of header ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

bool checkFlag(int flag, int bit){
	return ((flag>>bit) & 1) == 1;
}


void main() {
	vec3 worldPos = ( modelViewMatrix * vec4(vertexPosition, 1.0)).xyz;
	vec3 worldNormal = normalize(vec3(modelViewMatrix * ( vec4(normalPosition, 0.0))));
	vec3 cameraNormal = cameraPos - worldPos;
	vec3 lightVector = normalize( sunPos - worldPos );
	vec3 lightReflect = reflect( lightVector,  worldNormal);

	vec4 kd;
	if(checkFlag(used_textures_flag, KD_USED)){
		kd = texture( map_kd, vec2(texturePosition.y, 1-texturePosition.x));
		if(texturePosition.x < .5 && texturePosition.y < .5){
			FragColor = vec4(1,1,1,1);
		}else if(texturePosition.x >= .5 && texturePosition.y < .5){
			FragColor = vec4(1,0,0,1);
		}else if(texturePosition.x < .5 && texturePosition.y >= .5){
			FragColor = vec4(0,1,0,1);
		}else{
			FragColor = vec4(0,0,0,1);
		}
		//FragColor = vec4(texturePosition.xy, 0,1);
	}else{
		kd = material_kd;
		FragColor = vec4(0,1,0,1);
	}

	float brightnessScalar = dot(worldNormal, lightVector);
	brightnessScalar = ((brightnessScalar-1) *.5) +1;

	float specular = clamp(-dot(cameraNormal, lightReflect), 0.0, 1.0);
	brightnessScalar *= 1+specular;

	FragColor = kd *  brightnessScalar;
	//FragColor = vec4(normalize(normalPosition)*.5+.5, 1);//* brightness;
	//FragColor = vec4(brightness, brightness, brightness, 1);
	
}