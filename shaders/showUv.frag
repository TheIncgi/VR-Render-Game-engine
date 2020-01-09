#version 450

in  vec3 vertex;
in  vec2 uv;
in vec3 norm;
out vec4 FragColor;


void main() {
	FragColor = vec4(uv.xy, (uv.x*uv.y), 1.0);
}