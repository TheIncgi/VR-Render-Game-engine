#version 450
out vec4 FragColor;
uniform vec4 color;

void main() {
	FragColor = vec4(color.rgb, 1.0);
}