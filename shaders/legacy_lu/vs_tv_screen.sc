$input a_position, a_normal, a_texcoord0, a_texcoord1, a_color0
$output v_normal, v_texcoord0, v_texcoord1, v_color0, v_worldPos

#include <bgfx_shader.sh>

uniform vec4 u_luEffectTime;

void main()
{
    vec4 localPos = vec4(a_position, 1.0);
    gl_Position = mul(u_modelViewProj, localPos);
    v_normal = a_normal;
    v_texcoord0 = a_texcoord0;
    v_texcoord1 = vec2(sin(5.0 * ((2.0 * sin(u_luEffectTime.x)) - a_texcoord0.y)), 0.0);
    v_color0 = a_color0;
    v_worldPos = mul(u_model[0], localPos);
}
