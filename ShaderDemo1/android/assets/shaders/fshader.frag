#version 130

varying vec2 v_texCoord;
varying vec4 v_overlay;
varying float v_fade;

uniform sampler2D u_texture;

void main() {
  vec4 color = texture2D(u_texture, v_texCoord);
  vec3 final_color = mix(color.rgb, v_overlay.rgb, v_overlay.a);
  gl_FragColor = vec4(final_color, color.a * v_fade);
}