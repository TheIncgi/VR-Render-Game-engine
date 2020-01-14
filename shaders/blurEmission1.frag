#version 450
//out vec4 FragColor;
layout(location=0)out vec4 FragColor;
layout(location=1)out vec4 EmissionColor;
in vec2 UV;
uniform sampler2D renderedTexture;
uniform sampler2D emissionTexture;
uniform float uptime;
uniform int stage; //render pipeline step
float weight[5] = float[] (0.227027, 0.1945946, 0.1216216, 0.054054, 0.016216);

vec4 nearH(sampler2D tex, vec2 pos){
	vec2 tex_offset = 1.0 / textureSize(emissionTexture, 0); // gets size of single texel
    vec3 result = texture(emissionTexture, UV).rgb * weight[0]; // current fragment's contribution
    int w = 8;
    if(stage%2==0)
    {
        for(int i = 1; i < 5; ++i)
        {
            result += texture(emissionTexture, UV + vec2(tex_offset.x * i*w, 0.0)).rgb * weight[i];
            result += texture(emissionTexture, UV - vec2(tex_offset.x * i*w, 0.0)).rgb * weight[i];
        }
    }
    else
    {
        for(int i = 1; i < 5; ++i)
        {
            result += texture(emissionTexture, UV + vec2(0.0, tex_offset.y * i*w)).rgb * weight[i];
            result += texture(emissionTexture, UV - vec2(0.0, tex_offset.y * i*w)).rgb * weight[i];
        }
    }
    return vec4(result, 1.0);
}

// const float offset = .05;
void main() {
	// vec2 uv = UV;
 	// vec4 c = vec4(0);
  // 	c += 5.0 * texture2D(emissionTexture, uv - offset);
  // 	c += 6.0 * texture2D(emissionTexture, uv);
  // 	c += 5.0 * texture2D(emissionTexture, uv + offset);
  // 	c /= 16.0;
	FragColor     = texture( renderedTexture, UV);//texture(emissionTexture, UV + 0.025*vec2( sin(uptime+1024.0*UV.x),cos(uptime+768.0*UV.y)) );
	EmissionColor = nearH(emissionTexture, UV);
	//FragColor = vec4(UV.xy,0,1);
}