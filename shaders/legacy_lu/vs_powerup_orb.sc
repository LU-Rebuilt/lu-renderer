$input a_position, a_normal, a_texcoord0, a_texcoord1, a_color0
$output v_normal, v_texcoord0, v_texcoord1, v_color0, v_worldPos, v_reflectVector, v_vdn, v_diffuse, v_specular

#include <bgfx_shader.sh>

uniform vec4 u_luLightDirFade;
uniform vec4 u_luLightColorShadow;
uniform vec4 u_luAmbient;
uniform vec4 u_luCameraPos;

void main()
{
    vec4 localPos = vec4(a_position, 1.0);
    vec4 worldPos = mul(u_model[0], localPos);
    vec3 normal = normalize(mul(u_model[0], vec4(a_normal, 0.0)).xyz);
    vec3 lightVec = normalize(u_luLightDirFade.xyz);
    float ldn = dot(lightVec, normal);
    vec4 powerupColor = vec4(1.0, 0.0, 1.0, 1.0);
    vec3 viewDir = normalize(u_luCameraPos.xyz - worldPos.xyz);
    vec3 halfDir = normalize(viewDir + lightVec);
    float hdn = pow(max(dot(halfDir, normal), 0.0), 65.0);

    gl_Position = mul(u_modelViewProj, localPos);
    v_normal = normal;
    v_texcoord0 = a_texcoord0;
    v_texcoord1 = a_texcoord1;
    v_color0 = vec4_splat(1.0);
    v_worldPos = worldPos;
    v_diffuse = max(vec4_splat(0.0), powerupColor * ldn) + powerupColor * u_luAmbient;
    v_diffuse.a = 0.85 - abs(dot(viewDir, normal));
    v_specular = vec4((ldn * hdn) * u_luLightColorShadow.rgb, 1.0);
    v_reflectVector = reflect(viewDir, normal);
    v_vdn = abs(dot(viewDir, normal));
}
