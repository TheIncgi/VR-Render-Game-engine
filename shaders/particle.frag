#version 450
layout(location=0)out vec4 FragColor;
layout(location=1)out vec4 EmissionColor;


in vec3 vertexPosition;
in vec2 texturePosition;
in vec3 normalPosition;

uniform vec3 particlePosition;
uniform vec4 particleColor;
uniform vec3 particleScale;
uniform vec3 particleVelocity;
uniform int particleAge;
uniform int particleMaxAge;
uniform int particleEmissionStrength;
uniform int flags;
uniform sampler2D particleTexture;

bool checkFlag(int flag, int bit){
	return ((flag>>bit) & 1) == 1;
}

void main(){
	vec4 color = particleColor;
	if(checkFlag(flags,0)){
		color *= texture(particleTexture, texturePosition);
	}
	FragColor     = color;
	EmissionColor = vec4(color.xyz, particleEmissionStrength);
}