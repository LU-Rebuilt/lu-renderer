$input a_position, a_normal, a_tangent, a_bitangent, a_texcoord0, a_color0
$output v_texcoord0, v_color0, v_reflectVector, v_diffuseExtra

#include <bgfx_shader.sh>

uniform vec4 u_luLightDirFade;
uniform vec4 u_luCameraPos;
uniform vec4 u_luEffectTime;
uniform vec4 u_luUvMotion1;

void main()
{
    vec4 localPos = vec4(a_position, 1.0);
    vec4 worldPos = mul(u_model[0], localPos);
    vec3 tangent = normalize(mul(u_model[0], vec4(a_tangent, 0.0)).xyz);
    vec3 bitangent = normalize(mul(u_model[0], vec4(a_bitangent, 0.0)).xyz);
    vec3 normal = normalize(mul(u_model[0], vec4(a_normal, 0.0)).xyz);
    vec3 lightVector = normalize(u_luLightDirFade.xyz);
    vec3 viewVector = normalize(u_luCameraPos.xyz - worldPos.xyz);

    gl_Position = mul(u_modelViewProj, localPos);
    v_color0 = a_color0;
    v_texcoord0 = a_texcoord0 + (u_luUvMotion1.xy * u_luEffectTime.x * u_luEffectTime.y);
    v_diffuseExtra = vec3(dot(lightVector, tangent), dot(lightVector, bitangent), dot(lightVector, normal));
    v_reflectVector = vec3(dot(viewVector, tangent), dot(viewVector, bitangent), dot(viewVector, normal));
}
