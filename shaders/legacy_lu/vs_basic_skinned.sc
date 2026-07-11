$input a_position, a_normal, a_indices, a_weight, a_texcoord0, a_texcoord1, a_color0
$output v_normal, v_texcoord0, v_texcoord1, v_color0, v_worldPos

#include <bgfx_shader.sh>

uniform mat4 u_skinBones[64];
uniform vec4 u_materialDiffuse;
uniform vec4 u_lightDirAmbient;
uniform vec4 u_lightColor;
uniform vec4 u_luShaderFlags;
uniform vec4 u_luEffectTime;
uniform vec4 u_luUvMotion1;

void main()
{
    vec4 localPos = vec4(a_position, 1.0);
    float weightSum = dot(a_weight, vec4_splat(1.0));
    vec4 skinnedPos = localPos;
    vec3 skinnedNormal = a_normal;
    if (weightSum > 0.00001) {
        vec4 p0 = mul(u_skinBones[int(a_indices.x + 0.5)], localPos);
        vec4 p1 = mul(u_skinBones[int(a_indices.y + 0.5)], localPos);
        vec4 p2 = mul(u_skinBones[int(a_indices.z + 0.5)], localPos);
        vec4 p3 = mul(u_skinBones[int(a_indices.w + 0.5)], localPos);
        skinnedPos = (p0 * a_weight.x) + (p1 * a_weight.y) + (p2 * a_weight.z) + (p3 * a_weight.w);
        vec3 n0 = mul(u_skinBones[int(a_indices.x + 0.5)], vec4(a_normal, 0.0)).xyz;
        vec3 n1 = mul(u_skinBones[int(a_indices.y + 0.5)], vec4(a_normal, 0.0)).xyz;
        vec3 n2 = mul(u_skinBones[int(a_indices.z + 0.5)], vec4(a_normal, 0.0)).xyz;
        vec3 n3 = mul(u_skinBones[int(a_indices.w + 0.5)], vec4(a_normal, 0.0)).xyz;
        skinnedNormal = normalize((n0 * a_weight.x) + (n1 * a_weight.y) + (n2 * a_weight.z) + (n3 * a_weight.w));
    }

    gl_Position = mul(u_viewProj, skinnedPos);
    v_normal = skinnedNormal;
    v_texcoord0 = a_texcoord0 + (u_luUvMotion1.xy * u_luEffectTime.x * u_luEffectTime.y);
    v_texcoord1 = a_texcoord1;
    v_worldPos = skinnedPos;

    vec4 sourceColor = vec4(1.0, 1.0, 1.0, 1.0);
    sourceColor = mix(sourceColor, a_color0, u_luShaderFlags.y);
    sourceColor = mix(sourceColor, u_materialDiffuse, u_luShaderFlags.z);
    vec3 lightDir = normalize(-u_lightDirAmbient.xyz);
    float lightContribution = max(dot(lightDir, skinnedNormal), 0.0);
    vec3 litColor = (u_lightColor.rgb * lightContribution) + vec3_splat(u_lightDirAmbient.w);
    sourceColor.rgb = mix(sourceColor.rgb, sourceColor.rgb * litColor, u_luEffectTime.w);
    v_color0 = sourceColor;
}
