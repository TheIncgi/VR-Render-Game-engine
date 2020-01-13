#version 450
//out vec4 FragColor;
layout(location=0)out vec4 FragColor;
in vec2 UV;
uniform sampler2D renderedTexture;
uniform sampler2D emissionTexture;
uniform float uptime;

// vec4 near(sampler2D tex, vec2 pos){
// 	int r = 8;
// 	float step = .005;
// 	float t = r*step;
// 	//vec4[2*r] z = vec4(0,0,0,0);
// 	for(float x = -t; x<=t; x+=step){
// 		//z
// 	}
// 	for(float y = -.04; y<=.04; y+=.005){
// 			z += texture(tex, pos+vec2(x,y));
// 	}
// 	return vec4((z/128).xyz, 1)/2;
// }

const float offset = .05;
void main() {
	vec2 uv = UV;
 	// vec4 c = vec4(0);
  // 	c += 5.0 * texture2D(emissionTexture, uv - offset);
  // 	c += 6.0 * texture2D(emissionTexture, uv);
  // 	c += 5.0 * texture2D(emissionTexture, uv + offset);
  // 	c /= 16.0;
	FragColor = texture( renderedTexture, UV);// + near(emissionTexture, uv);//texture(emissionTexture, UV + 0.025*vec2( sin(uptime+1024.0*UV.x),cos(uptime+768.0*UV.y)) );
}