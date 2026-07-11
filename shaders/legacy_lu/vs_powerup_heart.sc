$input a_position, a_normal, a_texcoord0, a_texcoord1, a_color0
$output v_normal, v_texcoord0, v_texcoord1, v_color0, v_worldPos

#include <bgfx_shader.sh>

uniform vec4 u_luLightDirFade;
uniform vec4 u_luAmbient;

void main()
{
    vec4 localPos = vec4(a_position, 1.0);
    vec3 worldNormal = mul(u_model[0], vec4(a_normal, 0.0)).xyz;
    float ldn = dot(u_luLightDirFade.xyz, worldNormal);
    vec4 powerupColor = vec4(1.0, 0.0, 1.0, 1.0);

    gl_Position = mul(u_modelViewProj, localPos);
    v_normal = worldNormal;
    v_texcoord0 = a_texcoord0;
    v_texcoord1 = a_texcoord1;
    v_color0 = max(vec4_splat(0.0), powerupColor * ldn) + powerupColor * u_luAmbient;
    v_color0.a = 1.0;
    v_worldPos = mul(u_model[0], localPos);
}
