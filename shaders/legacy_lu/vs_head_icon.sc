$input a_position, a_normal, a_texcoord0, a_texcoord1, a_color0
$output v_normal, v_texcoord0, v_texcoord1, v_color0, v_worldPos

#include <bgfx_shader.sh>

uniform vec4 u_luEffectTime;
uniform vec4 u_luAmbient;

void main()
{
    vec3 displaced = a_position + a_normal * (abs(cos(u_luEffectTime.x)) * 0.05);
    vec4 localPos = vec4(displaced, 1.0);
    gl_Position = mul(u_modelViewProj, localPos);
    v_normal = a_normal;
    v_texcoord0 = a_texcoord0;
    v_texcoord1 = a_texcoord1;
    v_color0 = vec4(vec3(1.0, 1.0, 0.0) * (vec3_splat(1.0) + u_luAmbient.rgb), 1.0);
    v_worldPos = mul(u_model[0], localPos);
}
