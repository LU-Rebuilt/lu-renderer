$input a_position, a_normal, a_texcoord0, a_texcoord1, a_color0
$output v_normal, v_texcoord0, v_texcoord1, v_color0, v_worldPos

#include <bgfx_shader.sh>

uniform vec4 u_lightDirAmbient;
uniform vec4 u_lightColor;
uniform vec4 u_luEffectTime;
uniform vec4 u_luUvMotion1;
uniform vec4 u_luUvMotion2;

void main()
{
    vec4 localPos = vec4(a_position, 1.0);
    vec4 worldPos = mul(u_model[0], localPos);
    vec3 normal = normalize(mul(u_model[0], vec4(a_normal, 0.0)).xyz);
    float ldn = dot(normalize(-u_lightDirAmbient.xyz), normal);
    vec3 lighting = max(vec3_splat(0.0), u_lightColor.rgb * ldn) + vec3_splat(u_lightDirAmbient.w);

    gl_Position = mul(u_modelViewProj, localPos);
    v_normal = normal;
    v_texcoord0 = a_texcoord0 + (u_luUvMotion1.xy * u_luEffectTime.x * u_luEffectTime.y);
    v_texcoord1 = a_texcoord0 + (u_luUvMotion2.xy * u_luEffectTime.x * u_luEffectTime.y);
    v_color0 = vec4(lighting * a_color0.rgb, a_color0.a);
    v_worldPos = worldPos;
}
