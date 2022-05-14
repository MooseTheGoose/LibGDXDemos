

attribute vec2 a_position;
attribute vec2 a_texCoord;
attribute vec4 a_overlay;
attribute float a_fade;

uniform mat4 u_projTrans;

varying vec2 v_texCoord;
varying vec4 v_overlay;
varying float v_fade;

void main() {
  v_overlay = a_overlay;
  v_fade = a_fade;
  v_texCoord = a_texCoord;
  gl_Position = u_projTrans * vec4(a_position, 0.0f, 1.0f);
}